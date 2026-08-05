package com.example.Marketten.controller;

import com.example.Marketten.dto.list.ToneListRequest;
import com.example.Marketten.dto.list.ToneListResponse;
import com.example.Marketten.service.ToneListService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tone")
@RequiredArgsConstructor
public class ToneController {

    private final ToneListService toneListService;

    // 사용자용 - 전체 톤 리스트 조회
    @GetMapping
    public ResponseEntity<List<ToneListResponse>> getAllTones() {
        return ResponseEntity.ok(toneListService.getAllTones());
    }

    // 관리자용 - 새로운 톤 추가
    @PostMapping
    public ResponseEntity<ToneListResponse> createTone(@RequestBody ToneListRequest request) {
        return ResponseEntity.ok(toneListService.createTone(request));
    }

    // 관리자용 - 기존 톤 수정
    @PatchMapping("/{toneId}")
    public ResponseEntity<ToneListResponse> updateTone(@PathVariable Long toneId,
                                                       @RequestBody ToneListRequest request) {
        return ResponseEntity.ok(toneListService.updateTone(toneId, request));
    }

    // 관리자용 - 톤 삭제
    @DeleteMapping("/{toneId}")
    public ResponseEntity<Void> deleteTone(@PathVariable Long toneId) {
        toneListService.deleteTone(toneId);
        return ResponseEntity.noContent().build();
    }
}
