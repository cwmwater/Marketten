package com.example.Marketten.service;

import com.example.Marketten.domain.User;
import com.example.Marketten.dto.auth.TokenInfo;
import com.example.Marketten.dto.auth.TokenRefreshRequest;
import com.example.Marketten.dto.login.LoginRequest;

public interface LoginService {

    /**
     * 이메일/비밀번호로 사용자를 인증하고 토큰 정보를 반환합니다.
     */
    TokenInfo authenticateAndGenerateToken(LoginRequest request);

    /**
     * User 객체를 기반으로 토큰 정보를 생성하고, 온보딩 필요 여부를 결정합니다.
     * 이 메서드가 토큰 생성의 유일한 창구 역할을 합니다.
     */
    TokenInfo generateTokenForUser(User user);

    /**
     * 이메일로 User 객체를 조회합니다. (외부 서비스에서 사용)
     */
    User getUserByEmail(String email);

    /**
     * 이메일로 Refresh Token 값을 조회합니다. (외부 서비스에서 사용)
     */
    String getRefreshTokenByEmail(String email);

    /**
     * Refresh Token을 재발급합니다.
     */
    TokenInfo reissue(TokenRefreshRequest request);

    /**
     * 로그아웃을 처리합니다.
     */
    void logout(TokenRefreshRequest request);

    void updateLastLogin(String email);
}
