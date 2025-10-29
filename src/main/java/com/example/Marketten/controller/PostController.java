package com.example.Marketten.controller;

import com.example.Marketten.dto.post.PostResponse;
import com.example.Marketten.dto.post.PostUpdateRequest;
import com.example.Marketten.security.CustomUserDetails;
import com.example.Marketten.service.PostCreateService;
import com.example.Marketten.dto.post.PostSummaryDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostCreateService postCreateService;

    // 최종글 조회
    @GetMapping("/{postId}")
    public ResponseEntity<PostResponse> getPost(@PathVariable Long postId) {
        PostResponse response = postCreateService.getPost(postId);
        return ResponseEntity.ok(response);
    }

    // 최종글 수정
    @PatchMapping("/{postId}")
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable Long postId,
            @RequestBody PostUpdateRequest request) {

        PostResponse response = postCreateService.updatePost(postId, request);
        return ResponseEntity.ok(response);
    }

    // 최종글 삭제
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable Long postId) {
        postCreateService.deletePost(postId);
        return ResponseEntity.noContent().build();
    }

    // 현재 로그인한 사용자의 모든 게시글 목록 조회 (단계 정보 포함)
    @GetMapping("/user")
    public ResponseEntity<List<PostSummaryDTO>> getPostsByUser(
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        List<PostSummaryDTO> responses = postCreateService.getPostsByEmail(currentUser.getUsername());
        return ResponseEntity.ok(responses);
    }
}
