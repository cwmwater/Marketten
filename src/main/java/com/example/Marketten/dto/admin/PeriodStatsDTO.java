package com.example.Marketten.dto.admin;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PeriodStatsDTO {
    private List<ChartPointDTO> visitorStats; // 방문자 수 (선 그래프용)
    private List<ChartPointDTO> postStats;    // 글 비율 (원 그래프용)
}