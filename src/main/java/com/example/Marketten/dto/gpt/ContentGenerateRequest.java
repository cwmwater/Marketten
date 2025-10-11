package com.example.Marketten.dto.gpt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private String tonePreview;
    private String keywords;
}
