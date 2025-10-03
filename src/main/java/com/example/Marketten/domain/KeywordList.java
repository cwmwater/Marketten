package com.example.Marketten.domain;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "keyword_list")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KeywordList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long keywordId; //제목 고유 아이디

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "input_id", nullable = false)
    private TempPost tempPost; //임시 저장 아이디 FK

    @Column(length = 255, nullable = false)
    private String keywordName;

    private Integer keywordSearchValue;
}
