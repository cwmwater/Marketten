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
    private Integer keywordSearchValue; // 네이버 검색량
}
