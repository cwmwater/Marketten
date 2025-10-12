package com.example.Marketten.service;

import com.example.Marketten.domain.RefreshToken;
import com.example.Marketten.domain.User;
import com.example.Marketten.domain.VisitorLog; // ✨ import 추가
import com.example.Marketten.dto.login.LoginRequest;
import com.example.Marketten.dto.auth.TokenInfo;
import com.example.Marketten.dto.auth.TokenRefreshRequest;
import com.example.Marketten.dto.user.UserResponse;
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

    private static final long REFRESH_TOKEN_EXPIRE_SECONDS = 60 * 60 * 24 * 7;

    // 1. 일반 로그인
    @Override
    public TokenInfo authenticateAndGenerateToken(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이메일입니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 마지막 로그인 시간 업데이트
        user.setLastLoginAt(LocalDateTime.now());

        // 로그인 성공 시 방문 기록(VisitorLog) 저장
        VisitorLog log = VisitorLog.builder()
                .visitor(user)
                .visitDate(LocalDateTime.now()) // ✨ visitDate를 명시적으로 설정
                .build();
        visitorLogRepository.save(log);

        return generateTokenForUser(user);
    }

    // 2. 토큰 발급 공통 로직 (이하 코드는 변경 없음)
    @Override
    public TokenInfo generateTokenForUser(User user) {
        String accessToken = jwtUtil.generateAccessToken(user.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

        RefreshToken token = RefreshToken.builder()
                .id(user.getEmail())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiration(REFRESH_TOKEN_EXPIRE_SECONDS)
                .build();
        refreshTokenRepository.save(token);

        UserResponse userResponse = UserResponse.from(user);

        return TokenInfo.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .email(user.getEmail())
                .user(userResponse)
                .build();
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
        User user = userRepository.findByEmail(storedToken.getId())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        return generateTokenForUser(user);
    }

    @Override
    public void logout(TokenRefreshRequest request) {
        String email = jwtUtil.parseEmailFromToken(request.getRefreshToken());
        RefreshToken storedToken = refreshTokenRepository.findById(email)
                .orElseThrow(() -> new RuntimeException("유효하지 않은 Refresh Token입니다."));
        refreshTokenRepository.delete(storedToken);
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
    }

    @Override
    public String getRefreshTokenByEmail(String email) {
        RefreshToken token = refreshTokenRepository.findById(email)
                .orElseThrow(() -> new RuntimeException("Refresh Token을 찾을 수 없습니다."));
        return token.getRefreshToken();
    }
}