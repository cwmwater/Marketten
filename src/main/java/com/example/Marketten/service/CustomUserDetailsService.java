package com.example.Marketten.service;

import com.example.Marketten.domain.User;
import com.example.Marketten.repository.UserRepository;
import com.example.Marketten.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    // Spring Security가 인증을 시도할 때(로그인 시) 호출됩니다.
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        // 1. DB에서 이메일(Username)을 사용하여 사용자 정보 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("해당 이메일의 사용자(" + email + ")를 찾을 수 없습니다.")
                );

        // 2. 조회된 User 엔티티를 CustomUserDetails 객체로 변환하여 반환
        return new CustomUserDetails(user);
    }
}