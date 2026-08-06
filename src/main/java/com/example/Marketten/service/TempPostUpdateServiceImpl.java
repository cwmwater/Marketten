package com.example.Marketten.service;

import com.example.Marketten.domain.*;
import com.example.Marketten.dto.gpt.ContentGenerateRequest;
import com.example.Marketten.dto.gpt.ContentKeywordRequest;
import com.example.Marketten.dto.gpt.TitleGenerateRequest;
import com.example.Marketten.dto.gpt.TitleKeywordRequest;
import com.example.Marketten.dto.gpt.ToneExamplePayload;
import com.example.Marketten.dto.list.KeywordListDTO;
import com.example.Marketten.dto.list.TitleListDTO;
import com.example.Marketten.dto.temppost.TempPostRequest;
import com.example.Marketten.dto.temppost.TempPostResponce;
import com.example.Marketten.dto.temppost.TempPostUpdateRequest;
import com.example.Marketten.repository.FinalPostRepository;
import com.example.Marketten.repository.TempPostRepository;
import com.example.Marketten.repository.ToneExampleRepository;
import com.example.Marketten.repository.ToneListRepository;
import com.example.Marketten.repository.UserRepository;
import com.example.Marketten.util.FastApiClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
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
    private final ToneListRepository toneListRepository;
    private final ToneExampleRepository toneExampleRepository;
    private final ObjectMapper objectMapper;

    /** -------------------- 임시 저장글 생성 -------------------- */
    @Override
    public TempPostResponce createTempPost(TempPostRequest request, String userEmail){

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        FinalPost post = finalPostRepository.save(FinalPost.builder()
                .user(user)
                .finalTone("기본")
                .status("WRITING")
                .createdDate(LocalDateTime.now())
                .updatedDate(LocalDateTime.now())
                .build());

        TempPost temp = TempPost.builder()
                .post(post)
                .user(user)
                .productInfo(request.getProductInfo())
                .productFeatures(request.getProductFeatures())
                .userExperience(request.getUserExperience())
                .step(1)  // 초기 step은 1
                .selectedTone(request.getSelectedTone() != null ? request.getSelectedTone() : "STANDARD")
                .keywords(request.getKeywords())
                .updatedAt(LocalDateTime.now())
                .build();

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

        TempPost saved = tempPostRepository.save(temp);
        return toResponse(saved);
    }

    /** -------------------- Step 기반 업데이트 -------------------- */
    @Override
    public TempPostResponce updateTempPost(Long inputId, TempPostUpdateRequest request) {
        TempPost temp = tempPostRepository.findById(inputId)
                .orElseThrow(() -> new RuntimeException("TempPost not found"));

        // Step 값 결정
        Integer step = request.getStep();
        if (step != null) {
            temp.setStep(step);
        } else {
            step = temp.getStep();
        }

        // Step별 허용 필드 업데이트
        if (step == 1) {
            if (request.getProductInfo() != null) temp.setProductInfo(request.getProductInfo());
            if (request.getProductFeatures() != null) temp.setProductFeatures(request.getProductFeatures());
            if (request.getUserExperience() != null) temp.setUserExperience(request.getUserExperience());
            if (request.getSelectedTone() != null) temp.setSelectedTone(request.getSelectedTone());
            if (request.getKeywords() != null) temp.setKeywords(request.getKeywords());

            // 키워드 리스트 업데이트
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

            // FinalPost 동기화
            if (temp.getPost() != null && request.getSelectedTone() != null) {
                temp.getPost().setFinalTone(request.getSelectedTone());
                finalPostRepository.save(temp.getPost());
            }
        } else if (step == 2) {
            if (request.getGeneratedContent() != null) temp.setGeneratedContent(request.getGeneratedContent());
            if (request.getSelectedTone() != null) temp.setSelectedTone(request.getSelectedTone());
            if (request.getKeywords() != null) temp.setKeywords(request.getKeywords());
            if (request.getTitleKeywords() != null) temp.setTitleKeywords(request.getTitleKeywords());

            // 키워드 리스트
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

            // 제목 리스트
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

            // FinalPost 동기화
            if (temp.getPost() != null) {
                if (request.getGeneratedContent() != null) temp.getPost().setFinalContent(request.getGeneratedContent());
                if (request.getTitleKeywords() != null) temp.getPost().setFinalTitle(request.getTitleKeywords());
                if (request.getSelectedTone() != null) temp.getPost().setFinalTone(request.getSelectedTone());
                temp.getPost().setUpdatedDate(LocalDateTime.now());
                finalPostRepository.save(temp.getPost());
            }
        } else if (step == 3) {
            if (request.getGeneratedContent() != null) temp.setGeneratedContent(request.getGeneratedContent());
            if (request.getTitleKeywords() != null) temp.setTitleKeywords(request.getTitleKeywords());

            // FinalPost 동기화
            if (temp.getPost() != null) {
                if (request.getGeneratedContent() != null) temp.getPost().setFinalContent(request.getGeneratedContent());
                if (request.getTitleKeywords() != null) temp.getPost().setFinalTitle(request.getTitleKeywords());
                temp.getPost().setUpdatedDate(LocalDateTime.now());
                finalPostRepository.save(temp.getPost());
            }
        }

        temp.setUpdatedAt(LocalDateTime.now());
        return toResponse(tempPostRepository.save(temp));
    }


    /** -------------------- 단계별 임시 저장글 조회 -------------------- */
    @Override
    public TempPostResponce getTempPost(Long inputId) {
        TempPost temp = tempPostRepository.findById(inputId)
                .orElseThrow(() -> new RuntimeException("TempPost not found"));

        return toResponse(temp);
    }

    /** -------------------- Step2 액션 처리 -------------------- */
    @Override
    public TempPostResponce handleAction(Long inputId, String action, TempPostUpdateRequest request) {
        TempPost temp = tempPostRepository.findById(inputId)
                .orElseThrow(() -> new RuntimeException("TempPost not found"));

        // 먼저 Step 2 데이터 업데이트
        if (request.getProductInfo() != null) {
            temp.setProductInfo(request.getProductInfo());
        }
        if (request.getProductFeatures() != null) {
            temp.setProductFeatures(request.getProductFeatures());
        }
        if (request.getUserExperience() != null) {
            temp.setUserExperience(request.getUserExperience());
        }
        if (request.getSelectedTone() != null) {
            temp.setSelectedTone(request.getSelectedTone());
        }
        if (request.getGeneratedContent() != null) {
            temp.setGeneratedContent(request.getGeneratedContent());
        }
        if (request.getKeywords() != null) {
            temp.setKeywords(request.getKeywords());
        }
        if (request.getTitleKeywords() != null) {
            temp.setTitleKeywords(request.getTitleKeywords());
        }

        // 키워드 리스트 업데이트
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

        // 제목 리스트 업데이트
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

        // 액션 수행
        switch (action) {
            case "generateContent":
                ToneList selectedTone = toneListRepository.findByToneName(temp.getSelectedTone())
                        .orElse(null);
                String tonePreview = selectedTone != null ? selectedTone.getTonePreview() : "PREVIEW";

                // RAG 검색 대상: 선택된 톤에 등록된 예문 전체(텍스트+임베딩)
                List<ToneExamplePayload> toneExamples = selectedTone == null
                        ? Collections.emptyList()
                        : toneExampleRepository.findByTone_ToneId(selectedTone.getToneId()).stream()
                                .map(e -> ToneExamplePayload.builder()
                                        .text(e.getExampleText())
                                        .embedding(readEmbedding(e.getEmbedding()))
                                        .build())
                                .collect(Collectors.toList());

                ContentGenerateRequest contentReq = ContentGenerateRequest.builder()
                        .productInfo(temp.getProductInfo())
                        .productFeatures(temp.getProductFeatures())
                        .userExperience(temp.getUserExperience())
                        .selectedTone(temp.getSelectedTone())
                        .tonePreview(tonePreview)
                        .toneExamples(toneExamples)
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
                    Integer peak = stats.get("최대 수치") instanceof Number ? ((Number) stats.get("최대 수치")).intValue() : null;

                    KeywordList keyword = KeywordList.builder()
                            .tempPost(temp)
                            .keywordName(name)
                            .averageSearchValue(average)
                            .peakSearchValue(peak)
                            .build();
                    temp.getKeywordLists().add(keyword);
                });
                break;

            case "analyzeTitleKeywords":
                TitleKeywordRequest titleKeywordReq = TitleKeywordRequest.builder()
                        .generatedContent(temp.getGeneratedContent())
                        .build();
                Map<String, Object> titleKeywordMap = fastApiClient.analyzeTitleKeywords(titleKeywordReq);

                // 기존 keywordLists 초기화
                temp.getKeywordLists().clear();

                // titleKeywordMap 데이터를 keywordLists로 변환하여 추가
                titleKeywordMap.forEach((name, statsObj) -> {
                    Map<String, Object> stats = (Map<String, Object>) statsObj;
                    Integer average = stats.get("평균 수치") instanceof Number ? ((Number) stats.get("평균 수치")).intValue() : null;
                    Integer peak = stats.get("최대 수치") instanceof Number ? ((Number) stats.get("최대 수치")).intValue() : null;

                    KeywordList keyword = KeywordList.builder()
                            .tempPost(temp)
                            .keywordName(name)
                            .averageSearchValue(average)
                            .peakSearchValue(peak)
                            .build();
                    temp.getKeywordLists().add(keyword);
                });
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

        List<TitleListDTO> titles = temp.getTitleLists() != null ?
                temp.getTitleLists().stream()
                        .map(t -> TitleListDTO.builder()
                                .titleId(t.getTitleId())
                                .tempPostId(temp.getInputId())
                                .titleName(t.getTitleName())
                                .build())
                        .collect(Collectors.toList())
                : new ArrayList<>();

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
                : new ArrayList<>();

        return TempPostResponce.builder()
                .inputId(temp.getInputId())
                .postId(temp.getPost() != null ? temp.getPost().getPostId() : null)
                .productInfo(temp.getProductInfo())
                .productFeatures(temp.getProductFeatures())
                .userExperience(temp.getUserExperience())
                .keywords(temp.getKeywords())
                .titleKeywords(temp.getTitleKeywords())
                .generatedContent(temp.getGeneratedContent())
                .selectedTone(temp.getSelectedTone())
                .step(temp.getStep())
                .updatedAt(temp.getUpdatedAt())
                .titleList(titles)
                .keywordList(keywords)
                .build();
    }

    // ToneExample.embedding에 JSON 문자열로 저장된 벡터를 List<Double>로 역직렬화
    private List<Double> readEmbedding(String embeddingJson) {
        try {
            return objectMapper.readValue(embeddingJson, new com.fasterxml.jackson.core.type.TypeReference<List<Double>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("임베딩 역직렬화 실패: " + e.getMessage(), e);
        }
    }
}