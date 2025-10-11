package com.example.Marketten.service;


import com.example.Marketten.domain.TempPost;
import com.example.Marketten.dto.temppost.TempPostRequest;
import com.example.Marketten.dto.temppost.TempPostResponce;
import com.example.Marketten.dto.temppost.TempPostUpdateRequest;

public interface TempPostUpdateService {
    /**
     * 임시 저장글 생성
     */
    TempPostResponce createTempPost(TempPostRequest request, String userEmail);

    /**
     * 임시 저장글 수정 (step1/step2 모두 가능)
     */
    TempPostResponce updateTempPost(Long inputId, TempPostUpdateRequest request);

    /**
     * Step2 액션 처리
     * - 본문 생성 / 재생성 / 키워드 분석 / 제목 생성
     */
    TempPostResponce handleAction(Long inputId, String action, TempPostUpdateRequest request);

    /**
     * 단계별 임시 저장글 조회
     */
    TempPostResponce getTempPost(Long inputId);

    /**
     * 임시 저장글 삭제
     */
    void deleteTempPost(Long inputId);
}
