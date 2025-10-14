package com.example.Marketten.dto.admin;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CommonConfigRequestDTO {
    private String headerText;
    private String footerText;
    private String bannerImageUrl;
    // 나중에 새로운 설정이 추가되면 여기에 필드를 추가
}