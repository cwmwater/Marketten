package com.example.Marketten.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChartPointDTO {
    private String name;  // X축 레이블 (예: "10-16", "1주차", "10월")
    private long value; // Y축 값 (예: 방문자 수)
}