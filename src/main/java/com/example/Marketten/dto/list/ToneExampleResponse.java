package com.example.Marketten.dto.list;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ToneExampleResponse {
    private Long toneExampleId;
    private String exampleText;
}
