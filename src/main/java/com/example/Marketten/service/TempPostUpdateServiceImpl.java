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
import com.example.Marketten.repository.ToneListRepository;
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

    // 리포지토리 및 외부 서비스 의존성(주입)
    // tempPostRepository : 임시 저장글(TempPost) CRUD
    // finalPostRepository : 최종 저장글(FinalPost) CRUD 및 동기화
    // userRepository : 사용자 조회 (create 단계에서 사용자 확인용)
    // fastApiClient : 외부 FastAPI 연동 (콘텐츠 생성, 키워드 분석, 제목 생성 등)
    // toneListRepository : 톤의 미리보기(프리뷰) 조회용
    private final TempPostRepository tempPostRepository;
    private final FinalPostRepository finalPostRepository;
    private final UserRepository userRepository;
    private final FastApiClient fastApiClient;
    private final ToneListRepository toneListRepository;

    // 임시 저장글 생성 (초기화)
    // - 사용자를 이메일로 조회하고, FinalPost(최종 포스트) 엔티티를 초기값으로 생성하여 저장
    // - TempPost를 빌더로 구성하여 keyword list 등이 있을 경우 연관 엔티티 생성
    // - 반환은 DTO(TempPostResponce)
    @Override
    public TempPostResponce createTempPost(TempPostRequest request, String userEmail){

        // 1) user 존재 확인 — 없으면 예외 반환
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2) FinalPost 초기 생성 및 저장
        //    - finalTone: 기본값 "기본"
        //    - status: WRITING (작성중)
        //    - createdDate, updatedDate: 현재 시간
        FinalPost post = finalPostRepository.save(FinalPost.builder()
                .user(user)
                .finalTone("기본")
                .status("WRITING")
                .createdDate(LocalDateTime.now())
                .updatedDate(LocalDateTime.now())
                .build());

        // 3) TempPost 빌드 (연결된 FinalPost와 사용자 포함)
        //    - step 기본값 1
        //    - selectedTone이 null이면 "STANDARD"로 대체
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

        // 4) 전달된 keywordList가 있다면 KeywordList 엔티티로 변환하여 TempPost에 추가
        //    - 각 키워드는 temp와 연관관계를 가짐
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

        // 5) TempPost 저장 및 DTO 변환 반환
        TempPost saved = tempPostRepository.save(temp);
        return toResponse(saved);
    }

    // Step 기반 임시 저장글 업데이트
    // - 요청에 step 값이 있으면 해당 step으로 변경, 없으면 기존 step 유지
    // - step 값에 따라 허용된 필드만 업데이트(입력 검증 역할)
    // - step1: 기본 정보, 키워드 리스트, 톤 동기화
    // - step2: 생성된 콘텐츠, 제목 관련 데이터, FinalPost 동기화
    // - step3: 최종 콘텐츠/제목 키워드 동기화
    @Override
    public TempPostResponce updateTempPost(Long inputId, TempPostUpdateRequest request) {
        // 1) TempPost 조회 — 없으면 예외
        TempPost temp = tempPostRepository.findById(inputId)
                .orElseThrow(() -> new RuntimeException("TempPost not found"));

        // 2) step 결정 — request에 있으면 덮어쓰기
        Integer step = request.getStep();
        if (step != null) {
            temp.setStep(step);
        } else {
            step = temp.getStep();
        }

        // 3) step별 허용 필드 업데이트 처리
        if (step == 1) {
            // step1: 기본 입력 단계 — 제품 정보/특징/사용자 경험/톤/키워드 허용
            if (request.getProductInfo() != null) temp.setProductInfo(request.getProductInfo());
            if (request.getProductFeatures() != null) temp.setProductFeatures(request.getProductFeatures());
            if (request.getUserExperience() != null) temp.setUserExperience(request.getUserExperience());
            if (request.getSelectedTone() != null) temp.setSelectedTone(request.getSelectedTone());
            if (request.getKeywords() != null) temp.setKeywords(request.getKeywords());

            // 키워드 리스트가 제공되면 기존 리스트를 초기화하고 새로 추가
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

            // FinalPost와 동기화: 선택한 톤(selectedTone)이 전달되면 FinalPost.finalTone에 반영
            if (temp.getPost() != null && request.getSelectedTone() != null) {
                temp.getPost().setFinalTone(request.getSelectedTone());
                finalPostRepository.save(temp.getPost()); // DB 동기화
            }

        } else if (step == 2) {
            // step2: 콘텐츠 생성/편집 단계 — 생성된 콘텐츠, 제목 키워드 등 허용
            if (request.getGeneratedContent() != null) temp.setGeneratedContent(request.getGeneratedContent());
            if (request.getSelectedTone() != null) temp.setSelectedTone(request.getSelectedTone());
            if (request.getKeywords() != null) temp.setKeywords(request.getKeywords());
            if (request.getTitleKeywords() != null) temp.setTitleKeywords(request.getTitleKeywords());

            // 키워드 리스트 동기화(있으면 교체)
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

            // 제목 리스트 동기화(있으면 교체)
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

            // FinalPost 동기화: generatedContent, titleKeywords, selectedTone 등을 반영하고 updatedDate 갱신
            if (temp.getPost() != null) {
                if (request.getGeneratedContent() != null) temp.getPost().setFinalContent(request.getGeneratedContent());
                if (request.getTitleKeywords() != null) temp.getPost().setFinalTitle(request.getTitleKeywords());
                if (request.getSelectedTone() != null) temp.getPost().setFinalTone(request.getSelectedTone());
                temp.getPost().setUpdatedDate(LocalDateTime.now());
                finalPostRepository.save(temp.getPost()); // DB 동기화
            }

        } else if (step == 3) {
            // step3: 최종 확정 단계 — 최종 콘텐츠/제목 키워드만 허용
            if (request.getGeneratedContent() != null) temp.setGeneratedContent(request.getGeneratedContent());
            if (request.getTitleKeywords() != null) temp.setTitleKeywords(request.getTitleKeywords());

            // FinalPost 동기화: 최종 콘텐츠와 제목을 반영하고 updatedDate 갱신
            if (temp.getPost() != null) {
                if (request.getGeneratedContent() != null) temp.getPost().setFinalContent(request.getGeneratedContent());
                if (request.getTitleKeywords() != null) temp.getPost().setFinalTitle(request.getTitleKeywords());
                temp.getPost().setUpdatedDate(LocalDateTime.now());
                finalPostRepository.save(temp.getPost()); // DB 동기화
            }
        }

        // 공통: 업데이트 타임스탬프 갱신 및 저장
        temp.setUpdatedAt(LocalDateTime.now());
        return toResponse(tempPostRepository.save(temp));
    }

    // 단계별 임시 저장글 조회
    // - ID로 TempPost 조회 후 DTO로 변환하여 반환
    @Override
    public TempPostResponce getTempPost(Long inputId) {
        // 존재하지 않으면 예외
        TempPost temp = tempPostRepository.findById(inputId)
                .orElseThrow(() -> new RuntimeException("TempPost not found"));

        return toResponse(temp);
    }

    // Step2 관련 액션 처리 (generateContent, analyzeKeywords, analyzeTitleKeywords, generateTitles 등)
    // - action 문자열에 따라 외부 FastApiClient 호출 수행
    // - 호출 결과를 TempPost의 적절한 컬렉션/필드로 변환하여 저장
    // - 처리 전, request로 전달된 step2 관련 필드를 먼저 로컬 TempPost에 반영
    @Override
    public TempPostResponce handleAction(Long inputId, String action, TempPostUpdateRequest request) {
        // 1) TempPost 조회
        TempPost temp = tempPostRepository.findById(inputId)
                .orElseThrow(() -> new RuntimeException("TempPost not found"));

        // 2) Step 2에서 받을 수 있는 필드들 선반영(사용자가 보낸 최신 값으로 임시 객체 갱신)
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

        // 3) 키워드 리스트가 넘어오면 값을 교체
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

        // 4) 제목 리스트 교체
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

        // 5) action 분기 처리
        switch (action) {
            case "generateContent":
                // 5-1) tone preview(미리보기 텍스트)를 조회하여 ContentGenerateRequest에 포함
                //      - tone이 DB에 없으면 기본 "PREVIEW" 사용
                String tonePreview = toneListRepository.findByToneName(temp.getSelectedTone())
                        .map(ToneList::getTonePreview)
                        .orElse("PREVIEW");

                // 5-2) ContentGenerateRequest 빌드 — 핵심 입력값들을 외부 API로 전송
                ContentGenerateRequest contentReq = ContentGenerateRequest.builder()
                        .productInfo(temp.getProductInfo())
                        .productFeatures(temp.getProductFeatures())
                        .userExperience(temp.getUserExperience())
                        .selectedTone(temp.getSelectedTone())
                        .tonePreview(tonePreview)
                        .keywords(temp.getKeywords())
                        .build();

                // 5-3) 외부 API 호출 및 결과 저장
                String generated = fastApiClient.generateContent(contentReq);
                temp.setGeneratedContent(generated);
                break;

            case "analyzeKeywords":
                // 5-4) 콘텐츠(제품설명 등)를 분석하여 키워드 추천/통계 반환
                ContentKeywordRequest keywordReq = ContentKeywordRequest.builder()
                        .productInfo(temp.getProductInfo())
                        .productFeatures(temp.getProductFeatures())
                        .userExperience(temp.getUserExperience())
                        .build();

                // 5-5) 외부 API 반환값은 Map<String, Object> 형태라고 가정
                Map<String, Object> keywordMap = fastApiClient.analyzeContentKeywords(keywordReq);

                // 5-6) 기존 키워드 리스트 초기화 후 API 결과를 KeywordList 엔티티로 변환하여 추가
                temp.getKeywordLists().clear();
                keywordMap.forEach((name, statsObj) -> {
                    // statsObj를 Map으로 안전하게 캐스팅하고 키 값("평균 수치","최대 수치") 처리
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
                // 5-7) 생성된 콘텐츠를 바탕으로 제목 키워드 분석
                TitleKeywordRequest titleKeywordReq = TitleKeywordRequest.builder()
                        .generatedContent(temp.getGeneratedContent())
                        .build();
                Map<String, Object> titleKeywordMap = fastApiClient.analyzeTitleKeywords(titleKeywordReq);

                // 5-8) 기존 keywordLists 초기화(제목 키워드 결과를 키워드 리스트로 재활용)
                temp.getKeywordLists().clear();

                // 5-9) titleKeywordMap을 KeywordList로 변환하여 추가
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
                // 5-10) 외부 API로부터 제목 집합(중복 제거된 Set)을 받아 TitleList로 변환해 저장
                TitleGenerateRequest titleReq = TitleGenerateRequest.builder()
                        .generatedContent(temp.getGeneratedContent())
                        .keywords(temp.getKeywords())
                        .build();
                Set<String> titles = fastApiClient.generateTitles(titleReq);

                // 5-11) 기존 titleLists 초기화 후 추가
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
                // 지원하지 않는 액션 요청 시 IllegalArgumentException 발생
                throw new IllegalArgumentException("Unsupported action: " + action);
        }

        // 6) 변경된 내용 타임스탬프 갱신 및 저장
        temp.setUpdatedAt(LocalDateTime.now());
        TempPost saved = tempPostRepository.save(temp);
        return toResponse(saved);
    }

    // 임시 저장글 삭제
    // - 존재 여부 확인 후 삭제 처리
    @Override
    public void deleteTempPost(Long inputId) {
        TempPost temp = tempPostRepository.findById(inputId)
                .orElseThrow(() -> new RuntimeException("TempPost not found"));
        tempPostRepository.delete(temp);
    }

    // 엔티티(TempPost) -> DTO(TempPostResponce) 변환 유틸
    // - TitleList와 KeywordList를 DTO 리스트로 변환
    // - Null 안전성: 각 리스트가 null이면 빈 리스트로 반환
    private TempPostResponce toResponse(TempPost temp) {

        // TitleList 엔티티를 TitleListDTO로 변환 (null-safe)
        List<TitleListDTO> titles = temp.getTitleLists() != null ?
                temp.getTitleLists().stream()
                        .map(t -> TitleListDTO.builder()
                                .titleId(t.getTitleId())
                                .tempPostId(temp.getInputId())
                                .titleName(t.getTitleName())
                                .build())
                        .collect(Collectors.toList())
                : new ArrayList<>();

        // KeywordList 엔티티를 KeywordListDTO로 변환 (null-safe)
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

        // DTO 빌드: TempPost의 핵심 필드들을 응답 DTO에 매핑
        return TempPostResponce.builder()
                .inputId(temp.getInputId())
                .postId(temp.getPost() != null ? temp.getPost().getPostId() : null) // FinalPost가 연결되어 있으면 ID 포함
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
}
