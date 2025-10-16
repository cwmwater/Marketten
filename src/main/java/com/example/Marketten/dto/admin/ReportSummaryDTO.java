package com.example.Marketten.dto.admin;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReportSummaryDTO {
    private long totalUniqueVisitors; // 전체 순 방문자 수
    private long activeUsers;         // 현재 활성 사용자 수
    private long totalFinalPosts;     // 전체 최종 게시물 수
}