package com.example.Marketten.oauth2;

import java.util.Collections;
import java.util.Map;

// Naver 소셜 로그인에서 사용자 정보를 추출하는 클래스
public class NaverOAuth2UserInfo implements OAuth2UserInfo {
    private final Map<String, Object> attributes; // Naver에서 전달받은 사용자 정보

    public NaverOAuth2UserInfo(Map<String, Object> attributes) {
        // 이미 CustomOAuth2UserService에서 response를 꺼냈으므로 바로 저장
        this.attributes = attributes != null ? attributes : Collections.emptyMap();
    }

    @Override
    public String getId() {
        // 사용자 고유 ID 반환
        return (String) attributes.get("id");
    }

    @Override
    public String getEmail() {
        // 이메일 정보 반환
        return (String) attributes.get("email");
    }

    @Override
    public String getNickname() {
        // 사용자 이름(닉네임) 반환
        return (String) attributes.get("name");
    }

    @Override
    public String getImageUrl() {
        // 프로필 이미지 URL 반환
        return (String) attributes.get("profile_image");
    }
}
