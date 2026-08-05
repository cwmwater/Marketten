package com.example.Marketten.service;

import com.example.Marketten.domain.RefreshToken;
import com.example.Marketten.domain.Status;
import com.example.Marketten.domain.User;
import com.example.Marketten.domain.VisitorLog;
import com.example.Marketten.dto.auth.TokenInfo;
import com.example.Marketten.dto.auth.TokenRefreshRequest;
import com.example.Marketten.dto.login.LoginRequest;
import com.example.Marketten.dto.user.UserResponse;
import com.example.Marketten.repository.FinalPostRepository;
import com.example.Marketten.repository.RefreshTokenRepository;
import com.example.Marketten.repository.UserRepository;
import com.example.Marketten.repository.VisitorLogRepository;
import com.example.Marketten.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class LoginServiceImpl implements LoginService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final VisitorLogRepository visitorLogRepository;
    private final FinalPostRepository finalPostRepository;

    private static final long REFRESH_TOKEN_EXPIRE_SECONDS = 60 * 60 * 24 * 7; // 7일

    /**
     * 이메일/비밀번호로 사용자를 인증하고 토큰 정보를 반환합니다.
     */
    @Override
    public TokenInfo authenticateAndGenerateToken(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이메일입니다."));

        // 사용자 상태(탈퇴, 정지 등) 확인
        if (user.getStatus() != Status.ACTIVE) {
            log.warn("Login failed for non-active user: {}", request.getEmail());
            if (user.getStatus() == Status.DEACTIVATED) {
                throw new IllegalArgumentException("이미 탈퇴 처리된 계정입니다.");
            }
            throw new IllegalArgumentException("로그인할 수 없는 계정입니다.");
        }

        // 비밀번호 일치 확인
        if (user.getPassword() == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 마지막 로그인 시간 업데이트
        user.setLastLoginAt(LocalDateTime.now());

        // 방문 기록(로그) 저장
        VisitorLog log = VisitorLog.builder().visitor(user).visitDate(LocalDateTime.now()).build();
        visitorLogRepository.save(log);

        // 모든 토큰 생성 로직은 generateTokenForUser 메서드로 통일
        return generateTokenForUser(user);
    }

    /**
     * User 객체를 기반으로 토큰 정보를 생성하고, 글 작성 여부에 따라 온보딩 필요 여부를 결정합니다.
     */
    @Override
    public TokenInfo generateTokenForUser(User user) {
        String accessToken = jwtUtil.generateAccessToken(user.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

        boolean needsOnboarding = (finalPostRepository.countByUser(user) == 0) && !user.isTutorialCompleted();
        log.info("사용자: {}, 온보딩 필요 여부: {}", user.getEmail(), needsOnboarding);

        RefreshToken token = RefreshToken.builder()
                .id(user.getEmail())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiration(REFRESH_TOKEN_EXPIRE_SECONDS)
                .build();
        refreshTokenRepository.save(token);

        return TokenInfo.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .email(user.getEmail())
                .user(UserResponse.from(user))
                .needsOnboarding(needsOnboarding)
                .build();
    }

    /**
     * 사용자의 마지막 로그인 시간을 현재 시간으로 업데이트합니다. (소셜 로그인용)
     */
    @Override
    public void updateLastLogin(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + email));
        user.setLastLoginAt(LocalDateTime.now());
        // 이 메서드는 클래스 전체에 @Transactional이 적용되어 있으므로,
        // 메서드가 성공적으로 끝나면 변경된 lastLoginAt 값이 자동으로 DB에 UPDATE 됩니다.
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
    }

    @Override
    @Transactional(readOnly = true)
    public String getRefreshTokenByEmail(String email) {
        return refreshTokenRepository.findById(email)
                .map(RefreshToken::getRefreshToken)
                .orElseThrow(() -> new RuntimeException("Refresh Token을 찾을 수 없습니다."));
    }

    @Override
    public TokenInfo reissue(TokenRefreshRequest request) {
        String refreshToken = request.getRefreshToken();
        String email = jwtUtil.parseEmailFromToken(refreshToken);

        RefreshToken storedToken = refreshTokenRepository.findById(email)
                .orElseThrow(() -> new RuntimeException("유효하지 않거나 만료된 Refresh Token입니다."));

        if (!storedToken.getRefreshToken().equals(refreshToken)) {
            refreshTokenRepository.delete(storedToken);
            throw new RuntimeException("유효하지 않거나 변조된 Refresh Token입니다.");
        }

        refreshTokenRepository.delete(storedToken);
        User user = getUserByEmail(email);
        return generateTokenForUser(user);
    }

    @Override
    public void logout(TokenRefreshRequest request) {
        String email = jwtUtil.parseEmailFromToken(request.getRefreshToken());
        refreshTokenRepository.findById(email).ifPresent(refreshTokenRepository::delete);
    }
}

