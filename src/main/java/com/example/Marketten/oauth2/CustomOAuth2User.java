package com.example.Marketten.oauth2;

import com.example.Marketten.domain.Role;
import lombok.Getter;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

import java.util.Map;

/**
 * DefaultOAuth2User를 상속하고, email과 role 필드를 추가로 가진 사용자 정의 OAuth2User 클래스
 */
@Getter
public class CustomOAuth2User extends DefaultOAuth2User {

    private String email; // 사용자 이메일
    private Role role;    // 사용자 역할

    public CustomOAuth2User(Map<String, Object> attributes,
                            String nameAttributeKey,
                            String email,
                            Role role) {
        // 권한(authorities)은 사용하지 않으므로 빈 컬렉션 전달
        super(java.util.Collections.emptyList(), attributes, nameAttributeKey);
        this.email = email; // 이메일 설정
        this.role = role;   // 역할 설정
    }
}
