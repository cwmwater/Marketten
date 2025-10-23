package com.example.Marketten.dto.auth;

import com.example.Marketten.dto.user.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 로그인 및 토큰 재발급 응답 DTO
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenInfo {
    private String accessToken;
    private String refreshToken; // Refresh Token 추가
    private String email; // 토큰의 주체 (편의상 추가)

    // 로그인/회원가입/재발급 시 함께 반환할 사용자 정보
    private UserResponse user;
    private boolean needsOnboarding;
}
