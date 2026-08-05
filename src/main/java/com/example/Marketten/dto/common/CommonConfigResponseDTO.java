package com.example.Marketten.dto.common;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CommonConfigResponseDTO {
    private String mainTitle;
    private String mainSubtitle;
    private String callToActionTitle;

    private String footerCompanyName;
    private String footerAddress;
    private String footerEmail;
    private String footerCopyright;
}