package com.example.Marketten.service;

import com.example.Marketten.domain.SiteConfig;
import com.example.Marketten.dto.common.CommonConfigResponseDTO;
import com.example.Marketten.repository.SiteConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * CommonService의 실제 구현체 클래스입니다.
 * 사이트의 공통 설정 정보를 조회하는 로직을 담당하며,
 * 이 서비스는 주로 인증이 필요 없는 공개 정보를 제공합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 이 서비스의 메서드들은 모두 조회만 하므로 readOnly = true로 설정하여 성능을 최적화합니다.
public class CommonServiceImpl implements CommonService {

    private final SiteConfigRepository siteConfigRepository;

    /**
     * DB의 site_config 테이블에서 프론트엔드의 여러 페이지에서 필요한
     * 모든 공통 설정 값을 조회하여 하나의 DTO로 만들어 반환합니다.
     */
    @Override
    public CommonConfigResponseDTO getCommonConfig() {
        // --- 메인 페이지(MainPage.jsx)용 데이터 조회 ---
        String mainTitle = findConfigValue("MAIN_TITLE", "마케팅의 완성, 마케튼으로!");
        String mainSubtitle = findConfigValue("MAIN_SUBTITLE", "간단한 클릭만으로 효과적인 제품 홍보 블로그 글을 완성하세요!");
        String ctaTitle = findConfigValue("CTA_TITLE", "지금 시작해보세요");

        // --- 푸터(Footer.jsx) 및 설정 페이지(FooterSettings.jsx)용 데이터 조회 ---
        String companyName = findConfigValue("FOOTER_COMPANY_NAME", "마케튼(주)");
        String address = findConfigValue("FOOTER_ADDRESS", "중부대학교");
        String email = findConfigValue("FOOTER_EMAIL", "contact@marketten.kr");
        String copyright = findConfigValue("FOOTER_COPYRIGHT", "© 2025 Marketten Co. All rights reserved.");

        // 조회한 모든 값들을 하나의 DTO(CommonConfigResponseDTO)에 담아 반환합니다.
        return CommonConfigResponseDTO.builder()
                .mainTitle(mainTitle)
                .mainSubtitle(mainSubtitle)
                .callToActionTitle(ctaTitle)
                .footerCompanyName(companyName)
                .footerAddress(address)
                .footerEmail(email)
                .footerCopyright(copyright)
                .build();
    }

    /**
     * 특정 설정 키(Key)의 값(Value)을 안전하게 조회하는 헬퍼(도우미) 메서드입니다.
     * 데이터베이스에 해당 키가 존재하지 않을 경우, 미리 지정된 기본값을 대신 반환합니다.
     *
     * @param key          조회할 설정의 키 (예: "MAIN_TITLE")
     * @param defaultValue 해당 키가 DB에 없을 경우 사용할 기본값
     * @return 조회된 값 또는 기본값
     */
    private String findConfigValue(String key, String defaultValue) {
        return siteConfigRepository.findById(key)      // 1. 키로 SiteConfig 엔티티를 찾습니다. (반환값: Optional<SiteConfig>)
                .map(SiteConfig::getConfigValue)       // 2. 찾았다면(Optional이 비어있지 않다면), 그 안의 configValue 값을 꺼냅니다.
                .orElse(defaultValue);                 // 3. 만약 찾지 못했다면(Optional이 비어있다면), 대신 기본값을 반환합니다.
    }
}

