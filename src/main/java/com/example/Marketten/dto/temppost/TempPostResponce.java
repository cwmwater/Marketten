package com.example.Marketten.dto.temppost;

import com.example.Marketten.dto.list.KeywordListDTO;
import com.example.Marketten.dto.list.TitleListDTO;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class TempPostResponce {

    private Long inputId;              // 임시 저장 고유 ID
    private Long postId;               // 최종 글 ID (연결)
    private String productInfo;
    private String productFeatures;
    private String userExperience;
    private String keywords;
    private String titleKeywords;
    private String generatedContent;
    private String selectedTitle;
    private String selectedTone;
    private Integer step;
    private LocalDateTime updatedAt;

    // step2에서 생성된 후보 제목 리스트
    private List<TitleListDTO> titleList;

    private List<KeywordListDTO> keywordList;

}
