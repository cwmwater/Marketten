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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository; // 사용자 정보를 저장하고 조회하는 레포지토리
    private final PasswordEncoder passwordEncoder; // 비밀번호 암호화를 위한 인코더

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 기본 OAuth2UserService를 통해 사용자 정보 로드
        DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        // 어떤 소셜 로그인 제공자인지 확인 (google, kakao, naver 등)
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2UserInfo userInfo;
        String nameAttributeKey;

        // 사용자 속성 정보 추출
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // 제공자별로 사용자 정보 파싱
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
            throw new OAuth2AuthenticationException("Unsupported provider: " + registrationId); // 지원하지 않는 제공자 예외 처리
        }

        // 사용자 정보 추출
        String email = userInfo.getEmail();
        String nickname = userInfo.getNickname();
        String profileImage = userInfo.getImageUrl();

        // 이메일로 사용자 조회
        Optional<User> userOptional = userRepository.findByEmail(email);
        User user;

        if (userOptional.isPresent()) {
            // 기존 사용자일 경우 provider 정보 갱신
            user = userOptional.get();
            if (!user.getProvider().name().equalsIgnoreCase(registrationId)) {
                user.setProvider(SocialProvider.valueOf(registrationId.toUpperCase()));
            }
            userRepository.save(user); // 변경된 provider 저장
        } else {
            // 신규 사용자 생성 및 저장
            user = userRepository.save(
                    User.builder()
                            .email(email)
                            .provider(SocialProvider.valueOf(registrationId.toUpperCase()))
                            .nickname(nickname)
                            .imageUrl(profileImage)
                            .status(Status.ACTIVE)
                            .pwFlag(Boolean.TRUE)
                            .password(passwordEncoder.encode("SOCIAL_" + UUID.randomUUID())) // 랜덤 비밀번호 생성
                            .role(Role.USER)
                            .tempPost(0)
                            .clearPost(0)
                            .build()
            );
        }

        // 사용자 정보를 담은 CustomOAuth2User 객체 반환
        return new CustomOAuth2User(
                attributes,
                nameAttributeKey,
                user.getEmail(),
                user.getRole() // 사용자 역할 정보 전달
        );
    }
}
