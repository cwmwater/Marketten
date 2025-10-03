package com.example.Marketten.dto.post;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostUpdateRequest {
    private String finalTitle;
    private String finalContent;
    private String finalTone;
    private String status; // WRITING, COMPLETED 등 상태 변경 가능
}
