package com.example.Marketten.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId; // 유저 고유 아이디

    @Column(length = 255, nullable = false, unique = true)
    private String email; // 이메일

    @Column(length = 1000, nullable = false)
    private String password; // 해시화된 비밀번호

    @Column(length = 100, nullable = false)
    private String nickname; // 이름

    @Column(length = 1000)
    private String imageUrl; // 프로필 이미지 url

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private SocialProvider provider; // 로그인 구분

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private Status status; // 계정 상태

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private Role role; // 권한

    @Column(nullable = false)
    private Integer tempPost = 0; // 임시 저장 글 갯수

    @Column(nullable = false)
    private Integer clearPost = 0; // 작성 완료 글 갯수

    @Column(nullable = false)
    private LocalDateTime lastLoginAt; // 마지막 로그인 일시

    @Column(nullable = false)
    private Boolean pwFlag; // 비밀번호 랜덤 생성 플래그  1 : 소셜 로그인 0: 사이트 로그인

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now(); // 회원가입 일시

    // User → FinalPost (OneToMany)
    @OneToMany(mappedBy = "user")
    private List<FinalPost> finalPosts = new ArrayList<>();

    // User → VisitorLog (OneToMany)
    // User 삭제 시 VisitorLog는 남도록 cascade 생략
    @OneToMany(mappedBy = "visitor")
    private List<VisitorLog> visitorLogs = new ArrayList<>();
}
