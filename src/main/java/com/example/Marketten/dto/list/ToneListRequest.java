package com.example.Marketten.dto.list;



import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ToneListRequest {
    private String toneName;
    private String tonePreview;
}
