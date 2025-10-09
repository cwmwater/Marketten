package com.example.Marketten.service;

import com.example.Marketten.domain.RefreshToken;
import com.example.Marketten.domain.User;
import com.example.Marketten.dto.login.LoginRequest;
import com.example.Marketten.dto.auth.TokenInfo;
import com.example.Marketten.dto.auth.TokenRefreshRequest;
import com.example.Marketten.dto.user.UserResponse;
import com.example.Marketten.repository.RefreshTokenRepository;
import com.example.Marketten.repository.UserRepository;
import com.example.Marketten.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class LoginServiceImpl implements LoginService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    // Refresh Token의 만료 시간 (7일 기준, 초 단위로 Redis TTL 설정과 일치)
    private static final long REFRESH_TOKEN_EXPIRE_SECONDS = 60 * 60 * 24 * 7;

    // 1. 일반 로그인 (Controller에서 호출)
    @Override
    public TokenInfo authenticateAndGenerateToken(LoginRequest request) {

        // 1. 이메일로 사용자 조회
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이메일입니다."));

        // 2. 비밀번호 일치 검사
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 3. 마지막 로그인 시간 업데이트
        user.setLastLoginAt(LocalDateTime.now());

        // 4. 토큰 발급 및 응답 반환
        return generateTokenForUser(user);
    }

    // 2. 토큰 발급 공통 로직 (RegisterService에서도 호출됨)
    @Override
    public TokenInfo generateTokenForUser(User user) {
        // Access Token 및 Refresh Token 생성
        String accessToken = jwtUtil.generateAccessToken(user.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

        // RefreshToken을 Redis에 저장 (Key는 Email)
        RefreshToken token = RefreshToken.builder()
                .id(user.getEmail()) // Key: 이메일
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiration(REFRESH_TOKEN_EXPIRE_SECONDS) // 초 단위 TTL
                .build();

        refreshTokenRepository.save(token);

        // UserResponse DTO 생성 (마이페이지용 정보)
        UserResponse userResponse = UserResponse.from(user);

        // 최종 TokenInfo 생성 및 반환
        return TokenInfo.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .email(user.getEmail())
                .user(userResponse)
                .build();
    }

    /**
     * Refresh Token을 사용하여 Access Token과 Refresh Token을 재발급합니다.
     */
    @Transactional
    public TokenInfo reissue(TokenRefreshRequest request) {
        String refreshToken = request.getRefreshToken();

        // 1. Refresh Token 유효성 검사 및 Email 추출 (JWTUtil이 토큰 유효성 검사)
        String email = jwtUtil.parseEmailFromToken(refreshToken);

        // 2. Redis에서 Refresh Token 정보 확인 (ID = Email 사용)
        // Redis의 Key인 Email을 사용하여 조회합니다.
        RefreshToken storedToken = refreshTokenRepository.findById(email)
                .orElseThrow(() -> new RuntimeException("유효하지 않거나 만료된 Refresh Token입니다."));

        // 3. 저장된 토큰의 값과 요청 토큰의 값이 일치하는지 최종 확인
        if (!storedToken.getRefreshToken().equals(refreshToken)) {
            // 저장된 토큰과 다르면 (변조/이전 토큰) 삭제하고 예외 발생
            refreshTokenRepository.delete(storedToken);
            throw new RuntimeException("유효하지 않거나 변조된 Refresh Token입니다.");
        }

        // 4. 기존 Refresh Token 삭제 (사용 후 무효화)
        // 이메일 기반으로 조회했으므로, storedToken을 삭제합니다.
        refreshTokenRepository.delete(storedToken);

        // 5. 새로운 Access/Refresh 토큰 생성
        User user = userRepository.findByEmail(storedToken.getId())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 새로운 Access/Refresh 토큰 생성 및 Redis에 저장 (내부에서 generateTokenForUser 호출)
        TokenInfo newTokenInfo = generateTokenForUser(user);

        return newTokenInfo;
    }

    /**
     * 로그아웃: Refresh Token을 Redis에서 삭제하여 무효화합니다.
     */
    @Transactional
    public void logout(TokenRefreshRequest request) {
        // 1. 토큰에서 이메일(ID) 추출
        String email = jwtUtil.parseEmailFromToken(request.getRefreshToken());

        // 2. Redis에서 해당 토큰 엔티티를 찾습니다.
        RefreshToken storedToken = refreshTokenRepository.findById(email)
                .orElseThrow(() -> new RuntimeException("유효하지 않은 Refresh Token입니다."));

        // 3. Redis에서 해당 Refresh Token을 삭제하여 무효화합니다.
        refreshTokenRepository.delete(storedToken);
    }

    // 이메일로 사용자 정보를 가져오는 헬퍼 메서드 (AuthController에서 사용)
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
    }
}