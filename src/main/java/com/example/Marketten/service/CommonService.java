package com.example.Marketten.service;

import com.example.Marketten.dto.common.CommonConfigResponseDTO;

/**
 * 사이트 공통 설정 관련 비즈니스 로직을 처리하는 서비스 인터페이스입니다.
 * 이 서비스는 주로 인증이 필요 없는 공개 정보를 조회하는 데 사용됩니다.
 */
public interface CommonService {

    /**
     * 프론트엔드의 여러 페이지(메인, 푸터 등)에서 필요한
     * 모든 사이트 공통 설정 값들을 조회합니다.
     *
     * @return CommonConfigResponseDTO
     */
    CommonConfigResponseDTO getCommonConfig();
}

