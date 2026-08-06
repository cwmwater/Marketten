package com.example.Marketten.controller;

import com.example.Marketten.dto.list.ToneExampleRequest;
import com.example.Marketten.dto.list.ToneExampleResponse;
import com.example.Marketten.service.ToneExampleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tone/{toneId}/examples")
@RequiredArgsConstructor
public class ToneExampleController {

    private final ToneExampleService toneExampleService;

    // 특정 톤에 등록된 예문 전체 조회
    @GetMapping
    public ResponseEntity<List<ToneExampleResponse>> getExamples(@PathVariable Long toneId) {
        return ResponseEntity.ok(toneExampleService.getExamplesForTone(toneId));
    }

    // 관리자용 - 톤에 예문 추가 (등록 시 fastapi-module에서 임베딩 계산)
    @PostMapping
    public ResponseEntity<ToneExampleResponse> createExample(@PathVariable Long toneId,
                                                               @RequestBody ToneExampleRequest request) {
        return ResponseEntity.ok(toneExampleService.createExample(toneId, request));
    }

    // 관리자용 - 예문 삭제
    @DeleteMapping("/{exampleId}")
    public ResponseEntity<Void> deleteExample(@PathVariable Long toneId, @PathVariable Long exampleId) {
        toneExampleService.deleteExample(exampleId);
        return ResponseEntity.noContent().build();
    }
}
