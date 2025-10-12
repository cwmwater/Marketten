package com.example.Marketten.dto.admin;

import com.example.Marketten.domain.Role;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AdminUserDetailDTO {

    private Long userId;
    private String email;
    private String nickname;
    private String imageUrl;
    private Role role;
    private String provider; // 가입 경로 (예: "SITE", "GOOGLE")
    private String status;   // 사용자 상태 (예: "ACTIVE")
    private LocalDateTime createdAt;   // 가입일
    private LocalDateTime lastLoginAt; // 마지막 로그인 일시

    // 사용자가 작성한 글의 개수
    private long totalFinalPosts; // 최종 저장 글 수
    private long totalTempPosts;  // 임시 저장 글 수
}