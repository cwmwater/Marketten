package com.example.Marketten.dto.list;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ToneListResponse {
    private Long toneId;
    private String toneName;
    private String tonePreview;
}
