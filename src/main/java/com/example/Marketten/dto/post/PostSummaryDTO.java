package com.example.Marketten.dto.post;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PostSummaryDTO {
    private Long postId;
    private Long tempPostId;
    private String finalTitle;
    private int step;
    private String status; // "Complete", "Writing" 등 원본 상태
    private LocalDateTime createdDate;
}