package com.example.Marketten.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 비밀번호 변경 요청을 위한 DTO입니다.
 */
@Getter
@Setter
@NoArgsConstructor
public class UserPasswordUpdateRequest {

    @NotBlank(message = "기존 비밀번호는 필수 입력 항목입니다.")
    private String currentPassword; // 기존 비밀번호

    @NotBlank(message = "새 비밀번호는 필수 입력 항목입니다.")
    @Size(min = 8, max = 20, message = "새 비밀번호는 8자 이상 20자 이하로 입력해야 합니다.")
    private String newPassword; // 새 비밀번호
}