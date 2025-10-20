package com.example.Marketten.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tone_list")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ToneList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long toneId; // 톤 고유 아이디

    @Column(length = 50, nullable = false)
    private String toneName; // 톤 명

    @Column(length = 2000)
    private String tonePreview; //톤 미리보기 텍스트
}
