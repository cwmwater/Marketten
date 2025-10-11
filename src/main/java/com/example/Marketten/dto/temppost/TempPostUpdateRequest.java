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
public class TempPostUpdateRequest {


    // 수정 가능한 모든 필드
    private String productInfo;
    private String productFeatures;
    private String userExperience;

    private String keywords;
    private String titleKeywords;
    private String generatedContent;
    private String selectedTitle;
    private String selectedTone;

    private Integer step;

    private List<TitleListDTO> titleList;
    private List<KeywordListDTO> keywordList;
}
