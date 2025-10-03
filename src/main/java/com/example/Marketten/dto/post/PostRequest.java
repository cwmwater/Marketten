package com.example.Marketten.dto.post;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostRequest {
    private Long tempPostId;      // 임시 저장글 참조
    private String finalTitle;    // 최종글 제목
    private String finalContent;  // 최종글 본문
    private String finalTone;     // 톤 (선택 가능)
}
