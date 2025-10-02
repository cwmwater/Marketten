package com.example.Marketten.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "final_post")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinalPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postId; //최종글 고유 아이디

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; //유저 아이디 FK

    @Column(length = 255)
    private String finalTitle; //최종글 제목

    @Column(length = 5000)
    private String finalContent; //최종글 본문

    @Column(length = 50, nullable = false)
    private String finalTone = "STANDARD"; //톤

    @Column(length = 20, nullable = false)
    private String status = "WRITING"; // 작업 진행도

    @Column(nullable = false)
    private LocalDateTime createdDate = LocalDateTime.now(); // 생성 일자

    @Column(nullable = false)
    private LocalDateTime updatedDate = LocalDateTime.now(); // 수정 일자

    // 최종 글 삭제 시 관련 임시저장글도 삭제
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TempPost> tempPosts = new ArrayList<>();
}
