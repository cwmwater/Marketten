package com.example.Marketten.dto.admin;

import com.example.Marketten.dto.user.UserResponse; // 일반 사용자 정보 DTO 임포트
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 관리자 페이지 사용자 리스트 조회 응답 DTO입니다.
 * 페이징 정보와 UserResponse 리스트를 포함합니다.
 */
@Getter
@Setter
@Builder
public class AdminUserListResponse {

    private List<UserResponse> userList; // 사용자 정보 리스트
    private int currentPage;             // 현재 페이지 번호 (0부터 시작)
    private long totalItems;             // 총 항목 개수
    private int totalPages;              // 총 페이지 수
}