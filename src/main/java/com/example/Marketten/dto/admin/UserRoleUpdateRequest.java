package com.example.Marketten.dto.admin;

import com.example.Marketten.domain.Role;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor // JSON 데이터를 객체로 변환하기 위해 기본 생성자가 필요합니다.
public class UserRoleUpdateRequest {
    private Role newRole;
}