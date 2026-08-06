package com.example.Marketten.dto.gpt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentGenerateRequest {
    private String productInfo;
    private String productFeatures;
    private String userExperience;
    private String selectedTone;
    private String tonePreview; // 예문이 하나도 등록 안 된 톤일 때의 폴백
    private List<ToneExamplePayload> toneExamples; // RAG 검색 대상 예문 (텍스트+임베딩)
    private String keywords;
}
