package com.example.Marketten.dto.user;

import com.example.Marketten.domain.Role;
import com.example.Marketten.domain.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserResponse {

    private Long userId;
    private String email;
    private String nickname;
    private String imageUrl;
    private Role role;
    private LocalDateTime createdAt;

    // Entity를 DTO로 변환하는 정적 팩토리 메서드
    public static UserResponse from(User user) {
        return UserResponse.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .imageUrl(user.getImageUrl())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }
}