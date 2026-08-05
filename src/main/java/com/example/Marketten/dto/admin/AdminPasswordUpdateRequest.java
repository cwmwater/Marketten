package com.example.Marketten.dto.admin;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor // JSON -> Object 변환을 위한 기본 생성자
public class AdminPasswordUpdateRequest {
    private String currentPassword; // 현재 비밀번호
    private String newPassword;     // 새 비밀번호
}