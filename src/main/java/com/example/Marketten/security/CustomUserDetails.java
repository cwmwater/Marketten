package com.example.Marketten.security;

import com.example.Marketten.domain.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final User user;

    // --- UserDetails 인터페이스 구현 ---

    // 1. 사용자 권한 목록 반환
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // User 엔티티의 Role 필드를 권한 객체로 변환하여 반환합니다.
        // Role은 "ROLE_USER", "ROLE_ADMIN" 등의 문자열이어야 합니다.
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));

        // 💡 주의: User 도메인의 Role Enum에 name() 메서드가 있다고 가정합니다.
        // 예를 들어 Role.USER.name() -> "USER"가 되어 "ROLE_USER"로 생성됩니다.
    }

    // 2. 사용자의 비밀번호 반환
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    // 3. 사용자의 고유 ID (여기서는 이메일) 반환
    @Override
    public String getUsername() {
        return user.getEmail();
    }

    // 4. 계정 만료 여부 (true: 만료되지 않음)
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    // 5. 계정 잠금 여부 (true: 잠금되지 않음)
    @Override
    public boolean isAccountNonLocked() {
        // User.status 필드를 활용하여 계정 잠금 여부를 판단할 수 있습니다.
        // return user.getStatus() == Status.ACTIVE; 와 같은 로직을 구현할 수 있습니다.
        return true;
    }

    // 6. 비밀번호 만료 여부 (true: 만료되지 않음)
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // 7. 계정 활성화 여부 (true: 활성화됨)
    @Override
    public boolean isEnabled() {
        // User.status 필드를 활용하여 계정 활성화 여부를 판단할 수 있습니다.
        return true;
    }
}