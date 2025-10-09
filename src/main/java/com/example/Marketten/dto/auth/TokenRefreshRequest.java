package com.example.Marketten.dto.auth;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 토큰 재발급 및 로그아웃 요청 시 Refresh Token 값을 전달받는 DTO
// JSON 요청의 body에서 { "refreshToken": "토큰값" } 형태를 매핑합니다.
@Getter
@Setter
@NoArgsConstructor
public class TokenRefreshRequest {
    private String refreshToken;
}
