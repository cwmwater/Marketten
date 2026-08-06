package com.example.Marketten.service;

import com.example.Marketten.dto.list.ToneExampleRequest;
import com.example.Marketten.dto.list.ToneExampleResponse;

import java.util.List;

public interface ToneExampleService {

    // 특정 톤에 등록된 예문 전체 조회
    List<ToneExampleResponse> getExamplesForTone(Long toneId);

    // 관리자용 - 톤에 예문 추가 (등록 시 fastapi-module에서 임베딩 계산)
    ToneExampleResponse createExample(Long toneId, ToneExampleRequest request);

    // 관리자용 - 예문 삭제
    void deleteExample(Long toneExampleId);
}
