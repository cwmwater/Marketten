package com.example.Marketten.dto.admin;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VisitorStatsDTO {
    private long dailyVisitors;  // 오늘 순 방문자 수
    private long monthlyVisitors; // 이번 달 순 방문자 수
    private long yearlyVisitors;  // 올해 순 방문자 수
}