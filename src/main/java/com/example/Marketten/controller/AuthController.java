package com.example.Marketten.controller;

import com.example.Marketten.domain.User;
import com.example.Marketten.dto.auth.TokenInfo;
import com.example.Marketten.dto.auth.TokenRefreshRequest;
import com.example.Marketten.dto.login.LoginRequest; // 올바른 경로 (dto.login) 사용
import com.example.Marketten.dto.user.UserEmailPasswordResetRequest;
import com.example.Marketten.dto.user.UserPasswordUpdateRequest;
import com.example.Marketten.dto.user.UserRequest;
import com.example.Marketten.repository.UserRepository;
import com.example.Marketten.service.LoginService;
import com.example.Marketten.service.RegisterService;
import com.example.Marketten.service.LoginServiceImpl; // reissue, logout 메서드 호출용
import com.example.Marketten.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class    AuthController {

    private final RegisterService registerService;
    private final LoginService loginService;
    private final LoginServiceImpl loginServiceImpl; // 토큰 재발급/로그아웃 메서드 호출용 구현체
    private final UserRepository userRepository;
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<TokenInfo> register(@RequestBody @Valid UserRequest userRequest) {
        // 회원가입 후 자동 로그인 결과를 TokenInfo로 반환
        TokenInfo tokenInfo = registerService.registerNewUserAndLogin(userRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(tokenInfo);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenInfo> login(@RequestBody LoginRequest loginRequest) {
        // 로그인 성공 후 Access/Refresh Token을 포함한 TokenInfo 반환
        TokenInfo tokenInfo = loginService.authenticateAndGenerateToken(loginRequest);
        return ResponseEntity.ok(tokenInfo);
    }

    /**
     * Refresh Token을 사용하여 Access Token과 Refresh Token을 재발급합니다.
     * 경로: POST /api/auth/refresh
     */
    @PostMapping("/refresh")
    public ResponseEntity<TokenInfo> refresh(@RequestBody TokenRefreshRequest refreshRequest) {
        // LoginServiceImpl에서 토큰 갱신 로직 실행
        TokenInfo tokenInfo = loginServiceImpl.reissue(refreshRequest);
        return ResponseEntity.ok(tokenInfo);
    }

    /**
     * 로그아웃: Refresh Token을 Redis에서 삭제하여 무효화합니다.
     * 경로: POST /api/auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody TokenRefreshRequest logoutRequest) {
        // LoginServiceImpl에서 Refresh Token 삭제 로직 실행
        loginServiceImpl.logout(logoutRequest);
        // 성공 응답 (HTTP 200 OK)만 반환
        return ResponseEntity.ok().build();
    }

    @GetMapping("/check-email")
    public ResponseEntity<Boolean> checkEmail(@RequestParam String email) {
        boolean available = !userRepository.existsByEmail(email);
        return ResponseEntity.ok(available); // true: 사용 가능, false: 중복
    }

    @GetMapping("/check-nickname")
    public ResponseEntity<Boolean> checkNickname(@RequestParam String nickname) {
        boolean available = !userRepository.existsByNickname(nickname);
        return ResponseEntity.ok(available); // true: 사용 가능, false: 중복
    }

    @PatchMapping("/password-reset")
    public ResponseEntity<Void> resetPassword(@RequestBody @Valid UserEmailPasswordResetRequest request) {
        userService.resetPasswordByEmail(request.getEmail(), request.getNewPassword());
        return ResponseEntity.ok().build();
    }
}
