package com.example.Marketten.dto.admin;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CommonConfigRequestDTO {
    private String mainTitle;
    private String mainSubtitle;
    private String callToActionTitle;

    // 푸터 설정 페이지용 필드 추가
    private String footerCompanyName;
    private String footerAddress;
    private String footerEmail;
    private String footerCopyright;
}