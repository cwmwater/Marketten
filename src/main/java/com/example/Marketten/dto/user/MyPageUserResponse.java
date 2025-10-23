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
    private boolean passwordExists;
    private boolean needsOnboarding;
}
