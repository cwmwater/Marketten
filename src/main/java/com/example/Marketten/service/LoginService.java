package com.example.Marketten.service;

import com.example.Marketten.domain.User;
import com.example.Marketten.dto.auth.TokenInfo;
import com.example.Marketten.dto.auth.TokenRefreshRequest;
import com.example.Marketten.dto.login.LoginRequest;
import com.example.Marketten.dto.user.UserPasswordUpdateRequest;
import com.example.Marketten.dto.user.UserResponse;

public interface LoginService {

    // 마지막 로그인 시간을 업데이트하는 메서드 시그니처 추가
    void updateLastLogin(String email);

    // 이메일/비밀번호로 인증하고 토큰을 발급 (컨트롤러에서 사용)
    TokenInfo authenticateAndGenerateToken(LoginRequest request);

    // User 객체를 받아 토큰을 발급하는 공통 로직 (RegisterService에서 사용)
    TokenInfo generateTokenForUser(User user);

    // OAuth2SuccessHandler에서 사용: Refresh Token을 Redis에 저장하기 위해 User 객체를 조회
    User getUserByEmail(String email);

    // OAuth2SuccessHandler에서 사용: Refresh Token 값 조회를 위해
    String getRefreshTokenByEmail(String email);

    // ✨ Refresh Token 재발급 및 무효화 로직
    TokenInfo reissue(TokenRefreshRequest request);

    void logout(TokenRefreshRequest request);
}