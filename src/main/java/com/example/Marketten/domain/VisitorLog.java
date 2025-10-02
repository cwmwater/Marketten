package com.example.Marketten.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "visitor_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VisitorLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long visitLogId; // 방문자 로그 고유 아이디

    @Column(nullable = false)
    private LocalDateTime visitDate = LocalDateTime.now(); // 방문 일시

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "visitor",
            nullable = true,
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT) // FK 제약 제거
    )
    private User visitor; // 삭제 시 VisitorLog는 남음
}
