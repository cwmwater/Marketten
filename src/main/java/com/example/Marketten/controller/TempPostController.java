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

    /** -------------------- 임시 저장글 생성 -------------------- */
    @PostMapping("/{inputId}/{step}")
    public ResponseEntity<TempPostResponce> createTempPost(
            @PathVariable(required = false) Long inputId,
            @PathVariable Integer step,
            @RequestBody TempPostRequest request,
            @RequestHeader("email") String userEmail) { // 예: 이메일 헤더

        request.setStep(step);
        if (inputId != null) {
            request.setPostId(inputId);
        }

        TempPostResponce response = tempPostService.createTempPost(request, userEmail);
        return ResponseEntity.ok(response);
    }

    /** -------------------- 임시 저장글 수정 -------------------- */
    @PatchMapping("/{inputId}/{step}")
    public ResponseEntity<TempPostResponce> updateTempPost(
            @PathVariable Long inputId,
            @PathVariable Integer step,
            @RequestBody TempPostUpdateRequest request) {

        request.setStep(step); // step 세팅
        TempPostResponce response = tempPostService.updateTempPost(inputId, request);
        return ResponseEntity.ok(response);
    }

    /** -------------------- 임시 저장글 삭제 -------------------- */
    @DeleteMapping("/{inputId}")
    public ResponseEntity<Void> deleteTempPost(@PathVariable Long inputId) {
        tempPostService.deleteTempPost(inputId);
        return ResponseEntity.noContent().build();
    }

    /** -------------------- 임시 저장글 단계별 조회 -------------------- */
    @GetMapping("/{inputId}/{step}")
    public ResponseEntity<TempPostResponce> getTempPost(
            @PathVariable Long inputId,
            @PathVariable Integer step) {

        TempPostResponce response = tempPostService.getTempPost(inputId, step);
        return ResponseEntity.ok(response);
    }

    /** -------------------- Step2 액션 처리 (본문, 키워드, 제목 등) -------------------- */
    @PostMapping("/{inputId}/action/{action}")
    public ResponseEntity<TempPostResponce> handleStep2Action(
            @PathVariable Long inputId,
            @PathVariable String action,
            @RequestBody TempPostUpdateRequest request) {

        TempPostResponce response = tempPostService.handleStep2Action(inputId, action, request);
        return ResponseEntity.ok(response);
    }
}
