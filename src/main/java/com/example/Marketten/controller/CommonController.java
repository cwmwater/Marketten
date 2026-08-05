package com.example.Marketten.controller;

import com.example.Marketten.dto.common.CommonConfigResponseDTO;
import com.example.Marketten.service.CommonService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/common")
@RequiredArgsConstructor
public class CommonController {

    private final CommonService commonService;

    /**
     * 사이트 공통 설정 조회 API (누구나 접근 가능)
     * 경로: GET /api/common/config
     */
    @GetMapping("/config")
    public ResponseEntity<CommonConfigResponseDTO> getCommonConfig() {
        CommonConfigResponseDTO response = commonService.getCommonConfig();
        return ResponseEntity.ok(response);
    }
}