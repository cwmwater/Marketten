package com.example.Marketten.service;

import com.example.Marketten.domain.Role;
import com.example.Marketten.domain.Status;
import com.example.Marketten.domain.SocialProvider;
import com.example.Marketten.domain.User;
import com.example.Marketten.dto.auth.TokenInfo;
import com.example.Marketten.dto.login.LoginResponse;
import com.example.Marketten.dto.user.UserRequest;
import com.example.Marketten.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class RegisterServiceImpl implements RegisterService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final LoginService loginService; // 자동 로그인을 위한 LoginService 인터페이스 주입

    @Override
    public TokenInfo registerNewUserAndLogin(UserRequest request) {

        // 이메일 중복 검사
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        // 닉네임 중복 검사 (필요시)
        if (userRepository.existsByNickname(request.getNickname())) {
            throw new IllegalArgumentException("이미 존재하는 닉네임입니다.");
        }

        // User Entity 생성 및 비밀번호 암호화
        User newUser = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword())) // 비밀번호 해시화
                .nickname(request.getNickname())
                .imageUrl(request.getImageUrl() != null ? request.getImageUrl() : null)
                .provider(SocialProvider.SITE)
                .status(Status.ACTIVE)
                .role(Role.USER)
                .tempPost(0)
                .clearPost(0)
                .pwFlag(false) // 사이트 로그인
                .build();

        // DB 저장
        User savedUser = userRepository.save(newUser);

        // 저장된 사용자 정보로 자동 로그인 처리 (LoginService의 공통 메서드 사용)
        return loginService.generateTokenForUser(savedUser);
    }
}
