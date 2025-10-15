package com.example.Marketten.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 이메일 기반 비밀번호 재설정을 위한 DTO
 */
@Getter
@Setter
@NoArgsConstructor
public class UserEmailPasswordResetRequest {

    @NotBlank(message = "이메일은 필수 입력 항목입니다.")
    private String email;

    @NotBlank(message = "새 비밀번호는 필수 입력 항목입니다.")
    @Size(min = 8, max = 20, message = "새 비밀번호는 8자 이상 20자 이하로 입력해야 합니다.")
    private String newPassword;
}
