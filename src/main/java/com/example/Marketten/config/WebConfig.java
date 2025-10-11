package com.example.Marketten.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.util.UrlPathHelper;

/**
 * Spring MVC 설정을 커스터마이징하여 경로 매칭 및 URL 처리 방식을 수정합니다.
 * 특히 경로 변수에 점(.)이 포함된 이메일 주소를 허용하고,
 * PATCH/PUT 요청 시 Body를 올바르게 파싱하도록 설정합니다.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        // 경로 변수에서 점(.)을 파일 확장자로 인식하는 기본 설정을 비활성화합니다.
        configurer.setUseSuffixPatternMatch(false);

        // 경로의 마지막에 슬래시(/)가 있는 경우와 없는 경우를 동일하게 처리합니다.
        configurer.setUseTrailingSlashMatch(true);

        // URL 인코딩 및 디코딩 처리 방식 설정
        UrlPathHelper urlPathHelper = new UrlPathHelper();
        urlPathHelper.setUrlDecode(false); // URL 디코딩을 하지 않도록 설정
        configurer.setUrlPathHelper(urlPathHelper);
    }
}