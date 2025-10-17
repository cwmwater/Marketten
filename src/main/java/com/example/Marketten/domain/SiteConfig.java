package com.example.Marketten.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "site_config")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SiteConfig {

    @Id // 기본 키(Primary Key)
    @Column(name = "config_key", length = 50)
    private String configKey; // 설정 키 (예: "HEADER_TEXT", "FOOTER_COPYRIGHT")

    @Column(name = "config_value", length = 2000)
    private String configValue; // 실제 설정 값 (예: "마켓텐에 오신 것을 환영합니다.")

    public SiteConfig(String configKey, String configValue) {
        this.configKey = configKey;
        this.configValue = configValue;
    }

    // 값을 업데이트하기 위한 편의 메서드
    public void updateValue(String newValue) {
        this.configValue = newValue;
    }
}