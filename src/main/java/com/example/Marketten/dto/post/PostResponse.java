package com.example.Marketten.dto.post;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostResponse {
    private Long postId;
    private Long tempPostId;      // 연결된 임시 저장글
    private String finalTitle;
    private String finalContent;
    private String finalTone;
    private String status;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
}
