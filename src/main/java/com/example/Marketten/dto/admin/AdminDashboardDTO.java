package com.example.Marketten.dto.admin;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminDashboardDTO {
    private long totalUserCount;      // 전체 사용자 수
    private long totalPostCount;      // 전체 게시글 수
    private long tempPostCount;       // 임시 저장 글 수
    private long todayVisitorCount;   // 오늘 방문자 수 (오늘 로그인한 사용자 수)
    // 필요에 따라 다른 데이터 추가
}