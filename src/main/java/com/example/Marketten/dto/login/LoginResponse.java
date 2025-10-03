package com.example.Marketten.dto.login;

import com.example.Marketten.dto.user.UserResponse;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {

    private String accessToken; // JWT 인증 토큰 (자동 로그인 처리)
    private UserResponse user;  // 사용자 상세 정보 (마이페이지용)
}