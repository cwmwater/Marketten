package com.example.Marketten.service;

import com.example.Marketten.domain.Role;

import com.example.Marketten.domain.SocialProvider;
import com.example.Marketten.domain.Status;
import com.example.Marketten.domain.User;
import com.example.Marketten.oauth2.*;
import com.example.Marketten.oauth2.GoogleOAuth2UserInfo;
import com.example.Marketten.oauth2.KakaoOAuth2UserInfo;
import com.example.Marketten.oauth2.NaverOAuth2UserInfo;
import com.example.Marketten.oauth2.OAuth2UserInfo;
import com.example.Marketten.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2UserInfo userInfo;
        String nameAttributeKey;

        Map<String, Object> attributes = oAuth2User.getAttributes();

        if ("google".equalsIgnoreCase(registrationId)) {
            userInfo = new GoogleOAuth2UserInfo(attributes);
            nameAttributeKey = "sub";
        } else if ("kakao".equalsIgnoreCase(registrationId)) {
            userInfo = new KakaoOAuth2UserInfo(attributes);
            nameAttributeKey = "id";
        } else if ("naver".equalsIgnoreCase(registrationId)) {
            attributes = (Map<String, Object>) attributes.get("response");
            userInfo = new NaverOAuth2UserInfo(attributes);
            nameAttributeKey = "id";
        } else {
            throw new OAuth2AuthenticationException("Unsupported provider: " + registrationId);
        }

        // 사용자 정보 추출
        String email = userInfo.getEmail();
        String nickname = userInfo.getNickname();
        String profileImage = userInfo.getImageUrl();

        // DB 조회 및 신규 사용자 저장
        Optional<User> userOptional = userRepository.findByEmail(email);
        User user;

        if (userOptional.isPresent()) {
            // 기존 유저가 있으면 provider 갱신 및 로그인 시간 업데이트
            user = userOptional.get();
            if (!user.getProvider().name().equalsIgnoreCase(registrationId)) {
                user.setProvider(SocialProvider.valueOf(registrationId.toUpperCase()));
            }
            // 여기서 user.setLastLoginAt(LocalDateTime.now()) 등을 통해 로그인 시간을 갱신하는 로직이 필요할 수 있습니다.
            // 현재 엔티티에 setter가 없으므로 해당 로직이 있다면 User.java에 추가해야 합니다.
            userRepository.save(user); // provider 변경 저장
        } else {
            // 신규 유저 생성
            user = userRepository.save(
                    User.builder()
                            .email(email)
                            .provider(SocialProvider.valueOf(registrationId.toUpperCase()))
                            .nickname(nickname)
                            .imageUrl(profileImage)
                            .status(Status.ACTIVE)
                            .pwFlag(Boolean.TRUE)
                            .password(passwordEncoder.encode("SOCIAL_" + UUID.randomUUID())) // 랜덤 비밀번호
                            .role(Role.USER)
                            .tempPost(0)
                            .clearPost(0)
                            // created_at(LocalDateTime.now()) 제거 -> BaseEntity가 자동 처리
                            // lastLoginAt(LocalDateTime.now()) 제거 -> 필요하다면 User 엔티티에 메서드 추가 후 처리
                            .build()
            );
        }

        // CustomOAuth2User 생성
        return new CustomOAuth2User(
                attributes,
                nameAttributeKey,
                user.getEmail(),
                user.getRole() // 구독 타입만 전달
        );
    }
}
