package com.example.Marketten.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 사용자 정보 수정 요청을 위한 DTO입니다.
 * (비밀번호 수정은 별도의 엔드포인트에서 처리됨)
 */
@Getter
@Setter
@NoArgsConstructor
public class UserUpdateRequest {

    @NotBlank(message = "닉네임은 필수 입력 항목입니다.")
    @Size(min = 2, max = 10, message = "닉네임은 2자 이상 10자 이하로 입력해야 합니다.")
    private String nickname;

    // 프로필 이미지 URL은 선택 사항입니다.
    private String imageUrl;
}