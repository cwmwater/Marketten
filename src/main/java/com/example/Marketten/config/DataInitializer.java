package com.example.Marketten.config;

import com.example.Marketten.domain.SiteConfig;
import com.example.Marketten.repository.SiteConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final SiteConfigRepository siteConfigRepository;

    @Override
    public void run(String... args) throws Exception {
        log.info("데이터베이스 초기 설정을 확인합니다...");

        // 여기에 애플리케이션에 필요한 모든 기본 설정 값을 정의
        Map<String, String> initialConfigs = Map.of(
                "MAIN_TITLE", "마케팅의 시작, 마케튼으로!",
                "MAIN_SUBTITLE", "간단한 클릭만으로 제품 홍보 블로그 글을 작성해보세요!",
                "CTA_TITLE", "지금 시작해보세요",
                "FOOTER_COMPANY_NAME", "마케튼(주)",
                "FOOTER_ADDRESS", "중부대학교",
                "FOOTER_EMAIL", "contact@marketten.kr",
                "FOOTER_COPYRIGHT", "© 2025 Marketten Co. All rights reserved."
        );

        // 각 설정 값에 대해 DB에 존재하지 않으면 새로 생성합니다.
        initialConfigs.forEach(this::initializeConfig);

        log.info("데이터베이스 초기 설정 확인 완료.");
    }

    private void initializeConfig(String key, String defaultValue) {
        // DB에 해당 Key가 존재하지 않을 경우에만 새로 생성
        if (!siteConfigRepository.existsById(key)) {
            SiteConfig newConfig = new SiteConfig(key, defaultValue);
            siteConfigRepository.save(newConfig);
            log.info("기본 설정 값 생성: {} = {}", key, defaultValue);
        }
    }
}