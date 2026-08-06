package com.example.Marketten.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tone_example")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ToneExample {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long toneExampleId; // 예문 고유 아이디

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tone_id", nullable = false)
    private ToneList tone; // 소속 톤 FK

    @Column(length = 2000, nullable = false)
    private String exampleText; // 예문 텍스트

    // OpenAI text-embedding-3-small 벡터를 JSON 배열 문자열로 저장 (예: "[0.1,-0.2,...]")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String embedding;
}
