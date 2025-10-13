package com.example.Marketten.controller;

import com.example.Marketten.dto.temppost.TempPostRequest;
import com.example.Marketten.dto.temppost.TempPostResponce;
import com.example.Marketten.dto.temppost.TempPostUpdateRequest;
import com.example.Marketten.service.TempPostUpdateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/temp")
@RequiredArgsConstructor
public class TempPostController {

    private final TempPostUpdateService tempPostService;

    /**
     * -------------------- 임시 저장글 생성 --------------------
     */
    @PostMapping("/")
    public ResponseEntity<TempPostResponce> createTempPost(
            @RequestBody(required = false) TempPostRequest request,
            @RequestHeader("email") String userEmail) {

        // request가 null이면 빈 객체로 생성
        if (request == null) {
            request = TempPostRequest.builder().build();
        }

        TempPostResponce response = tempPostService.createTempPost(request, userEmail);
        return ResponseEntity.ok(response);
    }

    /**
     * -------------------- 임시 저장글 수정 --------------------
     */
    @PatchMapping("/{inputId}")
    public ResponseEntity<TempPostResponce> updateTempPost(
            @PathVariable Long inputId,
            @RequestBody TempPostUpdateRequest request) {

        TempPostResponce response = tempPostService.updateTempPost(inputId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * -------------------- 임시 저장글 삭제 --------------------
     */
    @DeleteMapping("/{inputId}")
    public ResponseEntity<Void> deleteTempPost(@PathVariable Long inputId) {
        tempPostService.deleteTempPost(inputId);
        return ResponseEntity.noContent().build();
    }

    /**
     * -------------------- 임시 저장글 조회 --------------------
     */
    @GetMapping("/{inputId}")
    public ResponseEntity<TempPostResponce> getTempPost(@PathVariable Long inputId) {
        TempPostResponce response = tempPostService.getTempPost(inputId);
        return ResponseEntity.ok(response);
    }

    /**
     * -------------------- 액션 처리 (본문, 키워드, 제목 등) --------------------
     */
    @PostMapping("/{inputId}/action/{action}")
    public ResponseEntity<TempPostResponce> handleAction(
            @PathVariable Long inputId,
            @PathVariable String action,
            @RequestBody TempPostUpdateRequest request) {

        TempPostResponce response = tempPostService.handleAction(inputId, action, request);
        return ResponseEntity.ok(response);
    }
}
