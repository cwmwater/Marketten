package com.example.Marketten.oauth2;

import java.util.Map;

// Google 소셜 로그인에서 사용자 정보를 추출하는 클래스
public class GoogleOAuth2UserInfo implements OAuth2UserInfo {
    private final Map<String, Object> attributes; // Google에서 전달받은 사용자 정보

    public GoogleOAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getId() {
        // Google의 고유 사용자 ID 반환
        return (String) attributes.get("sub");
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
        return (String) attributes.get("picture");
    }
}
