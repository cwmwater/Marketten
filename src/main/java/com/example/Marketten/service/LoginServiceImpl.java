package com.example.Marketten.service;


import com.example.Marketten.domain.User;
import com.example.Marketten.dto.login.LoginRequest;
import com.example.Marketten.dto.login.LoginResponse;
import com.example.Marketten.dto.user.UserResponse;
import com.example.Marketten.repository.UserRepository;
import com.example.Marketten.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class LoginServiceImpl implements LoginService { // ✨ LoginService 인터페이스 구현

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTUtil jwtUtil;

    // 1. 일반 로그인 (Controller에서 호출)
    @Override
    public LoginResponse authenticateAndGenerateToken(LoginRequest request) {

        // 1. 이메일로 사용자 조회
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이메일입니다."));

        // 2. 비밀번호 일치 검사
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 3. 마지막 로그인 시간 업데이트
        user.setLastLoginAt(LocalDateTime.now());
        // userRepository.save(user); // @Transactional이므로 별도로 save를 호출하지 않아도 변경이 반영되지만, 명시적으로 호출하는 것도 가능

        // 4. 토큰 발급 및 응답 반환
        return generateTokenForUser(user);
    }

    // 2. 토큰 발급 공통 로직 (RegisterService에서도 호출됨)
    @Override
    public LoginResponse generateTokenForUser(User user) {
        // JWT에 포함할 클레임
        Map<String, Object> claims = Map.of("email", user.getEmail());

        // Access Token 및 Refresh Token 생성
        String accessToken = jwtUtil.generateAccessToken(user.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

        // RefreshToken은 DB에 저장 (JWTUtil 내부에 로직이 있다고 가정)
        jwtUtil.updateRefreshToken(user.getEmail(), refreshToken);

        // UserResponse DTO 생성 (마이페이지용 정보)
        UserResponse userResponse = UserResponse.from(user);

        // 최종 LoginResponse 생성 및 반환
        return LoginResponse.builder()
                .accessToken(accessToken)
                .user(userResponse)
                .build();
    }
}
