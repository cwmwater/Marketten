package com.example.Marketten.service;


import com.example.Marketten.domain.*;
import com.example.Marketten.dto.gpt.ContentGenerateRequest;
import com.example.Marketten.dto.gpt.ContentKeywordRequest;
import com.example.Marketten.dto.gpt.TitleGenerateRequest;
import com.example.Marketten.dto.gpt.TitleKeywordRequest;
import com.example.Marketten.dto.list.KeywordListDTO;
import com.example.Marketten.dto.list.TitleListDTO;
import com.example.Marketten.dto.temppost.TempPostRequest;
import com.example.Marketten.dto.temppost.TempPostResponce;
import com.example.Marketten.dto.temppost.TempPostUpdateRequest;
import com.example.Marketten.repository.FinalPostRepository;
import com.example.Marketten.repository.TempPostRepository;
import com.example.Marketten.repository.UserRepository;
import com.example.Marketten.util.FastApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TempPostUpdateServiceImpl implements TempPostUpdateService {

    private final TempPostRepository tempPostRepository;
    private final FinalPostRepository finalPostRepository;
    private final UserRepository userRepository;
    private final FastApiClient fastApiClient;

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
                        .averageSearchValue(k.getAverageSearchValue())
                        .peakSearchValue(k.getPeakSearchValue())
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
                        .averageSearchValue(k.getAverageSearchValue())
                        .peakSearchValue(k.getPeakSearchValue())
                        .build();
                temp.getKeywordLists().add(keyword);
            });
        }

        temp.setUpdatedAt(LocalDateTime.now());
        return toResponse(tempPostRepository.save(temp));
    }

    /** -------------------- 단계별 임시 저장글 조회 -------------------- */
    @Override
    public TempPostResponce getTempPost(Long inputId) {
        TempPost temp = tempPostRepository.findById(inputId)
                .orElseThrow(() -> new RuntimeException("TempPost not found"));

        // step 체크 제거
        return toResponse(temp);
    }

    /** -------------------- Step2 액션 처리 -------------------- */
    @Override
    public TempPostResponce handleAction(Long inputId, String action, TempPostUpdateRequest request) {
        TempPost temp = tempPostRepository.findById(inputId)
                .orElseThrow(() -> new RuntimeException("TempPost not found"));

        // ✅ 먼저 TempPost 필드 수정
        temp.setProductInfo(request.getProductInfo());
        temp.setProductFeatures(request.getProductFeatures());
        temp.setUserExperience(request.getUserExperience());
        temp.setSelectedTone(request.getSelectedTone());
        temp.setStep(request.getStep());
        temp.setKeywords(request.getKeywords());
        temp.setTitleKeywords(request.getTitleKeywords());
        temp.setGeneratedContent(request.getGeneratedContent());
        temp.setSelectedTitle(request.getSelectedTitle());

        // ✅ 리스트도 먼저 갱신
        if (request.getKeywordList() != null) {
            temp.getKeywordLists().clear();
            request.getKeywordList().forEach(k -> {
                KeywordList keyword = KeywordList.builder()
                        .tempPost(temp)
                        .keywordName(k.getKeywordName())
                        .averageSearchValue(k.getAverageSearchValue())
                        .peakSearchValue(k.getPeakSearchValue())
                        .build();
                temp.getKeywordLists().add(keyword);
            });
        }

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

        // ✅ 수정된 상태를 기반으로 액션 수행
        switch (action) {
            case "generateContent":
                ContentGenerateRequest contentReq = ContentGenerateRequest.builder()
                        .productInfo(temp.getProductInfo())
                        .productFeatures(temp.getProductFeatures())
                        .userExperience(temp.getUserExperience())
                        .selectedTone(temp.getSelectedTone())
                        .tonePreview("PREVIEW")
                        .keywords(temp.getKeywords())
                        .build();
                String generated = fastApiClient.generateContent(contentReq);
                temp.setGeneratedContent(generated);
                break;

            case "analyzeKeywords":
                ContentKeywordRequest keywordReq = ContentKeywordRequest.builder()
                        .productInfo(temp.getProductInfo())
                        .productFeatures(temp.getProductFeatures())
                        .userExperience(temp.getUserExperience())
                        .build();
                Map<String, Object> keywordMap = fastApiClient.analyzeContentKeywords(keywordReq);
                temp.getKeywordLists().clear();
                keywordMap.forEach((name, statsObj) -> {
                    Map<String, Object> stats = (Map<String, Object>) statsObj;
                    Integer average = stats.get("평균 수치") instanceof Number ? ((Number) stats.get("평균 수치")).intValue() : null;
                    Integer peak = stats.get("최고 수치") instanceof Number ? ((Number) stats.get("최고 수치")).intValue() : null;

                    KeywordList keyword = KeywordList.builder()
                            .tempPost(temp)
                            .keywordName(name)
                            .averageSearchValue(average)
                            .peakSearchValue(peak)
                            .build();
                    temp.getKeywordLists().add(keyword);
                });
                temp.setKeywords(request.getKeywords()); // 선택 키워드만 저장
                break;

            case "analyzeTitleKeywords":
                TitleKeywordRequest titleKeywordReq = TitleKeywordRequest.builder()
                        .generatedContent(temp.getGeneratedContent())
                        .build();
                Map<String, Object> titleKeywordMap = fastApiClient.analyzeTitleKeywords(titleKeywordReq);
                temp.setTitleKeywords(titleKeywordMap.toString());
                break;

            case "generateTitles":
                TitleGenerateRequest titleReq = TitleGenerateRequest.builder()
                        .generatedContent(temp.getGeneratedContent())
                        .keywords(temp.getKeywords())
                        .build();
                Set<String> titles = fastApiClient.generateTitles(titleReq);
                temp.getTitleLists().clear();
                titles.forEach(t -> {
                    TitleList title = TitleList.builder()
                            .tempPost(temp)
                            .titleName(t)
                            .build();
                    temp.getTitleLists().add(title);
                });
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

        // TitleListDTO 변환 (null 체크 추가)
        List<TitleListDTO> titles = temp.getTitleLists() != null ?
                temp.getTitleLists().stream()
                        .map(t -> TitleListDTO.builder()
                                .titleId(t.getTitleId())
                                .tempPostId(temp.getInputId())
                                .titleName(t.getTitleName())
                                .build())
                        .collect(Collectors.toList())
                : new ArrayList<>();  // ← null이면 빈 리스트

        // KeywordListDTO 변환 (null 체크 추가)
        List<KeywordListDTO> keywords = temp.getKeywordLists() != null ?
                temp.getKeywordLists().stream()
                        .map(k -> KeywordListDTO.builder()
                                .keywordId(k.getKeywordId())
                                .tempPostId(temp.getInputId())
                                .keywordName(k.getKeywordName())
                                .averageSearchValue(k.getAverageSearchValue())
                                .peakSearchValue(k.getPeakSearchValue())
                                .build())
                        .collect(Collectors.toList())
                : new ArrayList<>();  // ← null이면 빈 리스트

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
