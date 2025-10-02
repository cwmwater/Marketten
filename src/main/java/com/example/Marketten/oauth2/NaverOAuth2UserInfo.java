package com.example.Marketten.oauth2;

import java.util.Collections;
import java.util.Map;

public class NaverOAuth2UserInfo implements OAuth2UserInfo {
    private final Map<String, Object> attributes;

    public NaverOAuth2UserInfo(Map<String, Object> attributes) {
        // 이미 CustomOAuth2UserService에서 response 꺼냈으므로 바로 저장
        this.attributes = attributes != null ? attributes : Collections.emptyMap();
    }

    @Override
    public String getId() { return (String) attributes.get("id"); }

    @Override
    public String getEmail() { return (String) attributes.get("email"); }

    @Override
    public String getNickname() { return (String) attributes.get("name"); }

    @Override
    public String getImageUrl() { return (String) attributes.get("profile_image"); }
}

