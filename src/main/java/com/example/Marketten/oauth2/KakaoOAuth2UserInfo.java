package com.example.Marketten.oauth2;

import java.util.Map;

// Kakao 소셜 로그인에서 사용자 정보를 추출하는 클래스
public class KakaoOAuth2UserInfo implements OAuth2UserInfo {
    private final Map<String, Object> attributes; // Kakao에서 전달받은 사용자 정보

    public KakaoOAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getId() {
        // 사용자 고유 ID 반환
        return attributes.get("id").toString();
    }

    @Override
    public String getEmail() {
        // 이메일 정보 추출
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        return kakaoAccount != null ? (String) kakaoAccount.get("email") : null;
    }

    @Override
    public String getNickname() {
        // 닉네임 정보 추출
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
        return profile != null ? (String) profile.get("nickname") : null;
    }

    @Override
    public String getImageUrl() {
        // 프로필 이미지 URL 추출
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
        return profile != null ? (String) profile.get("profile_image_url") : null;
    }
}
