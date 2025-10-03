package com.example.Marketten.service;

import com.example.Marketten.domain.*;
import com.example.Marketten.dto.list.KeywordListDTO;
import com.example.Marketten.dto.list.TitleListDTO;
import com.example.Marketten.dto.temppost.TempPostRequest;
import com.example.Marketten.dto.temppost.TempPostResponce;
import com.example.Marketten.dto.temppost.TempPostUpdateRequest;
import com.example.Marketten.repository.FinalPostRepository;
import com.example.Marketten.repository.TempPostRepository;
import com.example.Marketten.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TempPostUpdateServiceImpl implements TempPostUpdateService {

    private final TempPostRepository tempPostRepository;
    private final FinalPostRepository finalPostRepository;
    private final UserRepository userRepository;

    /** -------------------- 임시 저장글 생성 -------------------- */
    @Override
    public TempPostResponce createTempPost(TempPostRequest request, String userEmail){

        // 이메일로 사용자 조회
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // FinalPost 새로 생성
        FinalPost post = finalPostRepository.save(FinalPost.builder()
                .user(user)
                .finalTone("STANDARD")
                .status("WRITING")
                .createdDate(LocalDateTime.now())
                .updatedDate(LocalDateTime.now())
                .build());

        // TempPost 생성
        TempPost temp = TempPost.builder()
                .post(post)
                .productInfo(request.getProductInfo())
                .productFeatures(request.getProductFeatures())
                .userExperience(request.getUserExperience())
                .step(request.getStep())
                .generatedContent(request.getGeneratedContent())
                .selectedTitle(request.getSelectedTitle())
                .selectedTone(request.getSelectedTone())
                .keywords(request.getKeywords())
                .titleKeywords(request.getTitleKeywords())
                .updatedAt(LocalDateTime.now())
                .build();

        // KeywordList 매핑
        if (request.getKeywordList() != null) {
            request.getKeywordList().forEach(k -> {
                KeywordList keyword = KeywordList.builder()
                        .tempPost(temp)
                        .keywordName(k.getKeywordName())
                        .keywordSearchValue(k.getKeywordSearchValue())
                        .build();
                temp.getKeywordLists().add(keyword);
            });
        }

        // TitleList 매핑
        if (request.getTitleList() != null) {
            request.getTitleList().forEach(t -> {
                TitleList title = TitleList.builder()
                        .tempPost(temp)
                        .titleName(t.getTitleName())
                        .build();
                temp.getTitleLists().add(title);
            });
        }

        TempPost saved = tempPostRepository.save(temp);
        return toResponse(saved);
    }

    /** -------------------- 임시 저장글 수정 -------------------- */
    @Override
    public TempPostResponce updateTempPost(Long inputId, TempPostUpdateRequest request) {
        TempPost temp = tempPostRepository.findById(inputId)
                .orElseThrow(() -> new RuntimeException("TempPost not found"));

        // TempPost 필드 덮어쓰기
        temp.setProductInfo(request.getProductInfo());
        temp.setProductFeatures(request.getProductFeatures());
        temp.setUserExperience(request.getUserExperience());
        temp.setKeywords(request.getKeywords());
        temp.setTitleKeywords(request.getTitleKeywords());
        temp.setGeneratedContent(request.getGeneratedContent());
        temp.setSelectedTitle(request.getSelectedTitle());
        temp.setSelectedTone(request.getSelectedTone());
        temp.setStep(request.getStep());

        // TitleList 갱신
        if (request.getTitleList() != null) {
            temp.getTitleLists().clear();
            request.getTitleList().forEach(t -> {
                TitleList title = TitleList.builder()
                        .tempPost(temp)
                        .titleName(t.getTitleName())
                        .build();
                temp.getTitleLists().add(title);
            });
        }

        // KeywordList 갱신
        if (request.getKeywordList() != null) {
            temp.getKeywordLists().clear();
            request.getKeywordList().forEach(k -> {
                KeywordList keyword = KeywordList.builder()
                        .tempPost(temp)
                        .keywordName(k.getKeywordName())
                        .keywordSearchValue(k.getKeywordSearchValue())
                        .build();
                temp.getKeywordLists().add(keyword);
            });
        }

        temp.setUpdatedAt(LocalDateTime.now());
        return toResponse(tempPostRepository.save(temp));
    }

    /** -------------------- 단계별 임시 저장글 조회 -------------------- */
    @Override
    public TempPostResponce getTempPost(Long inputId, Integer step) {
        TempPost temp = tempPostRepository.findById(inputId)
                .orElseThrow(() -> new RuntimeException("TempPost not found"));

        if (step != null && !step.equals(temp.getStep())) {
            throw new RuntimeException("Step mismatch");
        }

        return toResponse(temp);
    }

    /** -------------------- Step2 액션 처리 -------------------- */
    @Override
    public TempPostResponce handleStep2Action(Long inputId, String action, TempPostUpdateRequest request) {
        TempPost temp = tempPostRepository.findById(inputId)
                .orElseThrow(() -> new RuntimeException("TempPost not found"));

        switch (action) {
            case "generateContent": // 본문 생성
            case "regenerate":      // 본문 재생성
                temp.setGeneratedContent(request.getGeneratedContent());
                break;
            case "analyzeKeywords": // 키워드 분석
                // KeywordListDTO를 엔티티로 변환해서 갱신
                if (request.getKeywordList() != null) {
                    temp.getKeywordLists().clear();
                    request.getKeywordList().forEach(k -> {
                        KeywordList keyword = KeywordList.builder()
                                .tempPost(temp)
                                .keywordName(k.getKeywordName())
                                .keywordSearchValue(k.getKeywordSearchValue())
                                .build();
                        temp.getKeywordLists().add(keyword);
                    });
                }
                break;
            case "generateTitles":  // 제목 생성
                // TitleListDTO를 엔티티로 변환해서 갱신
                if (request.getTitleList() != null) {
                    temp.getTitleLists().clear();
                    request.getTitleList().forEach(t -> {
                        TitleList title = TitleList.builder()
                                .tempPost(temp)
                                .titleName(t.getTitleName())
                                .build();
                        temp.getTitleLists().add(title);
                    });
                }
                temp.setTitleKeywords(request.getTitleKeywords());
                break;
            default:
                throw new IllegalArgumentException("Unsupported action: " + action);
        }

        temp.setUpdatedAt(LocalDateTime.now());
        TempPost saved = tempPostRepository.save(temp);
        return toResponse(saved);
    }

    /** -------------------- 임시 저장글 삭제 -------------------- */
    @Override
    public void deleteTempPost(Long inputId) {
        TempPost temp = tempPostRepository.findById(inputId)
                .orElseThrow(() -> new RuntimeException("TempPost not found"));
        tempPostRepository.delete(temp);
    }

    /** -------------------- 엔티티 → DTO 변환 -------------------- */
    private TempPostResponce toResponse(TempPost temp) {

        // TitleListDTO 변환
        List<TitleListDTO> titles = temp.getTitleLists().stream()
                .map(t -> TitleListDTO.builder()
                        .titleId(t.getTitleId())
                        .tempPostId(temp.getInputId())
                        .titleName(t.getTitleName())
                        .build())
                .collect(Collectors.toList());

        // KeywordListDTO 변환
        List<KeywordListDTO> keywords = temp.getKeywordLists().stream()
                .map(k -> KeywordListDTO.builder()
                        .keywordId(k.getKeywordId())
                        .tempPostId(temp.getInputId())
                        .keywordName(k.getKeywordName())
                        .keywordSearchValue(k.getKeywordSearchValue())
                        .build())
                .collect(Collectors.toList());

        return TempPostResponce.builder()
                .inputId(temp.getInputId())
                .postId(temp.getPost() != null ? temp.getPost().getPostId() : null)
                .productInfo(temp.getProductInfo())
                .productFeatures(temp.getProductFeatures())
                .userExperience(temp.getUserExperience())
                .keywords(temp.getKeywords())
                .titleKeywords(temp.getTitleKeywords())
                .generatedContent(temp.getGeneratedContent())
                .selectedTitle(temp.getSelectedTitle())
                .selectedTone(temp.getSelectedTone())
                .step(temp.getStep())
                .updatedAt(temp.getUpdatedAt())
                .titleList(titles)
                .keywordList(keywords)
                .build();
    }
}
