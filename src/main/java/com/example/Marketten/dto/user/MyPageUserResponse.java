package com.example.Marketten.dto.user;

import lombok.Builder;
import lombok.Getter;
import com.example.Marketten.domain.Role;

@Getter
@Builder
public class MyPageUserResponse {
    private Role role;
    private String nickname;
    private String imageUrl;
    private String provider; // "SITE", "GOOGLE" 등
    private String createdAt;
    private String lastLoginAt;
    private long totalFinalPosts;
    private long totalTempPosts;
    private boolean passwordExists; // 비밀번호 변경 시 체크
    private boolean needsOnboarding; // 온보딩 튜토리얼이 필요한 회원인지
}
