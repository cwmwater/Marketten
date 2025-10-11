package com.example.Marketten.dto.list;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class KeywordListDTO {
    private Long keywordId;
    private Long tempPostId; // TempPost FK
    private String keywordName;
    // 평균 검색량
    private Integer averageSearchValue;
    // 최고 검색량
    private Integer peakSearchValue;
}
