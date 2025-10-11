package com.example.Marketten.dto.temppost;

import com.example.Marketten.dto.list.KeywordListDTO;
import com.example.Marketten.dto.list.TitleListDTO;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class TempPostRequest {

    private Long postId; // 추가: 최종 글 ID
    // step1: 제품 정보 & 사용자 경험
    private String productInfo;
    private String productFeatures;
    private String userExperience;

    // step2: 키워드 선택, 본문 생성
    private String keywords;
    private String titleKeywords;
    private String generatedContent;
    private String selectedTitle;
    private String selectedTone;

    private Integer step; // 현재 단계

    // step2에서 생성된 후보 제목 리스트
    private List<TitleListDTO> titleList;

    private List<KeywordListDTO> keywordList;

}
