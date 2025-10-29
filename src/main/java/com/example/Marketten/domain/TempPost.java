package com.example.Marketten.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "temp_post")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TempPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long inputId; // 임시 저장 고유 아이디

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 작성자 정보 FK

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private FinalPost post; //최종 글 아이디 FK

    @Column(length = 500)
    private String productInfo; //제품명

    @Column(length = 2000)
    private String productFeatures; // 제품 특징

    @Column(length = 5000)
    private String userExperience; //사용자 경험

    @Column(length = 500)
    private String keywords; // 선택한 키워드

    @Column(length = 500)
    private String titleKeywords; // 선택된 제목

    @Column(length = 20, nullable = false)
    private String selectedTone = "기본"; // 선택한 톤

    private Integer step = 1; //현재 진행 페이지

    @Column(length = 5000)
    private String generatedContent; // 생성 글

    private LocalDateTime updatedAt = LocalDateTime.now(); // 수정 일시

    // TempPost 삭제 시 관련 제목 리스트도 삭제
    @OneToMany(mappedBy = "tempPost", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TitleList> titleLists = new ArrayList<>();

    // TempPost 삭제 시 관련 키워드 리스트도 삭제
    @OneToMany(mappedBy = "tempPost", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<KeywordList> keywordLists = new ArrayList<>();
}
