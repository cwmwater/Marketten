package com.example.Marketten.service;

import com.example.Marketten.dto.list.ToneListRequest;
import com.example.Marketten.dto.list.ToneListResponse;

import java.util.List;

public interface ToneListService {

    // 사용자용 - 전체 톤 리스트 조회
    List<ToneListResponse> getAllTones();

    // 관리자용 - 새로운 톤 추가
    ToneListResponse createTone(ToneListRequest request);

    // 관리자용 - 기존 톤 수정
    ToneListResponse updateTone(Long toneId, ToneListRequest request);

    // 관리자용 - 톤 삭제
    void deleteTone(Long toneId);
}
