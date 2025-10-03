package com.example.Marketten.controller;


import lombok.extern.slf4j.Slf4j;
import com.example.Marketten.dto.login.LoginRequest;
import com.example.Marketten.dto.login.LoginResponse;
import com.example.Marketten.dto.user.UserRequest;
import com.example.Marketten.service.LoginService;
import com.example.Marketten.service.RegisterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final RegisterService registerService;
    private final LoginService loginService;

    // 회원가입 API
    @PostMapping("/register")
    // 반환 타입을 LoginResponse로 변경하여 자동 로그인 처리
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody UserRequest userRequest) {

        // 회원가입 및 자동 로그인 처리 (JWT 토큰 발급 포함)
        LoginResponse response = registerService.registerNewUserAndLogin(userRequest);

        // 회원가입 성공 시 201 Created 응답과 함께 LoginResponse 반환
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 로그인 API
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {

        LoginResponse response = loginService.authenticateAndGenerateToken(loginRequest);

        return ResponseEntity.ok(response);
    }
}

