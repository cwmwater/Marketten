package com.example.Marketten.oauth2;

import com.example.Marketten.domain.Role;
import lombok.Getter;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

import java.util.Collection;
import java.util.Map;

/**
 * DefaultOAuth2User를 상속하고, email과 role 필드를 추가로 가진다.
 */
@Getter
public class CustomOAuth2User extends DefaultOAuth2User {

    private String email;
    private Role role;

    public CustomOAuth2User(Map<String, Object> attributes,
                            String nameAttributeKey,
                            String email,
                            Role role) {
        // 권한(authorities)은 사용하지 않으므로 빈 컬렉션 전달
        super(java.util.Collections.emptyList(), attributes, nameAttributeKey);
        this.email = email;
        this.role = role;
    }


}
