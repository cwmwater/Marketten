package com.example.Marketten.dto.gpt;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TitleKeywordRequest {
    private String generatedContent;
}
