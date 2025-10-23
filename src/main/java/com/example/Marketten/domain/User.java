package com.example.Marketten.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
@DynamicInsert // 값이 없는 필드는 DB의 기본값을 사용하도록 함
public class User extends BaseEntity { // BaseEntity를 상속받아 시간 필드를 자동 관리

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId; // ID 필드명을 'userId'로 유지

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(nullable = false, length = 100)
    private String password; // 암호화된 비밀번호 저장

    @Column(nullable = false, length = 20)
    private String nickname;

    @Column(name = "image_url")
    private String imageUrl; // 프로필 이미지 URL

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt; // 마지막 로그인 시간

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role; // 사용자 권한 (예: USER, ADMIN)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status; // 계정 상태 (예: ACTIVE, INACTIVE)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SocialProvider provider; // 가입 경로

    @Column(name = "pw_flag", nullable = false)
    private boolean pwFlag; // 비밀번호 변경 필요 여부

    @Column(name = "temp_post", nullable = false)
    private int tempPost; // 임시 저장된 게시물 수

    @Column(name = "clear_post", nullable = false)
    private int clearPost; // 최종 게시물 수

    @Column(name = "tutorial_completed", nullable = false)
    private boolean tutorialCompleted = false;

    // --- Setter 메서드 (UserServiceImpl에서 사용) ---

    /**
     * 비밀번호를 업데이트합니다. (비밀번호 수정 로직에서 사용됨)
     *
     * @param password 새 암호화된 비밀번호
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * 소셜 프로바이더를 업데이트합니다. (CustomOAuth2UserService에서 사용됨)
     *
     * @param provider 새로운 소셜 프로바이더
     */
    public void setProvider(SocialProvider provider) {
        this.provider = provider;
    }

    /**
     * 마지막 로그인 시간을 업데이트합니다. (로그인 서비스 로직에서 사용됨)
     *
     * @param lastLoginAt 현재 로그인 시간
     */
    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    /**
     * 닉네임을 업데이트합니다. (UserServiceImpl에서 사용됨)
     *
     * @param nickname 새로운 닉네임
     */
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    /**
     * 프로필 이미지 URL을 업데이트합니다. (UserServiceImpl에서 사용됨)
     *
     * @param imageUrl 새로운 이미지 URL
     */
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isTutorialCompleted() {
        return this.tutorialCompleted;
    }

    // --- 비즈니스 로직 메서드 ---

    /**
     * 일반 회원가입을 위한 User 엔티티 빌더입니다.
     */
    public static User registerUser(String email, String password, String nickname, String imageUrl, String encodedPassword) {
        return User.builder()
                .email(email)
                .password(encodedPassword)
                .nickname(nickname)
                .imageUrl(imageUrl)
                .role(Role.USER)
                .status(Status.ACTIVE)
                .provider(SocialProvider.SITE)
                .pwFlag(false)
                .tempPost(0)
                .clearPost(0)
                .build();
    }
}