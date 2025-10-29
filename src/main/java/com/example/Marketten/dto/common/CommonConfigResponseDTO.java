package com.example.Marketten.dto.common;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
// 모든 권한이 볼 수 있는 메인 배너, 푸터
public class CommonConfigResponseDTO {
    private String mainTitle;
    private String mainSubtitle;
    private String callToActionTitle;

    private String footerCompanyName;
    private String footerAddress;
    private String footerEmail;
    private String footerCopyright;
}