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
public class TitleGenerateRequest {
    private String keywords;
    private String generatedContent;
}
