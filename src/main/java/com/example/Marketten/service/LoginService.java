package com.example.Marketten.service;

import com.example.Marketten.domain.User;
import com.example.Marketten.dto.login.LoginRequest;
import com.example.Marketten.dto.login.LoginResponse;


public interface LoginService {

    // 이메일/비밀번호로 인증하고 토큰을 발급 (컨트롤러에서 사용)
    LoginResponse authenticateAndGenerateToken(LoginRequest request);

    // User 객체를 받아 토큰을 발급하는 공통 로직 (RegisterService에서 사용)
    LoginResponse generateTokenForUser(User user);
}