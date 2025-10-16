package com.example.Marketten.dto.admin;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminDashboardStatsDTO {
    private PeriodStatsDTO daily;   // 일간 통계
    private PeriodStatsDTO weekly;  // 주간 통계
    private PeriodStatsDTO monthly; // 월간 통계
    private PeriodStatsDTO yearly;  // 연간 통계
}