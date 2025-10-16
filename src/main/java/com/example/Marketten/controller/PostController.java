package com.example.Marketten.controller;

import com.example.Marketten.dto.post.PostResponse;
import com.example.Marketten.dto.post.PostUpdateRequest;
import com.example.Marketten.service.PostCreateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostCreateService postCreateService;

    /** -------------------- 최종글 조회 -------------------- */
    @GetMapping("/{postId}")
    public ResponseEntity<PostResponse> getPost(@PathVariable Long postId) {
        PostResponse response = postCreateService.getPost(postId);
        return ResponseEntity.ok(response);
    }

    /** -------------------- 최종글 수정 -------------------- */
    @PatchMapping("/{postId}")
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable Long postId,
            @RequestBody PostUpdateRequest request) {
        PostResponse response = postCreateService.updatePost(postId, request);
        return ResponseEntity.ok(response);
    }

    /** -------------------- 최종글 삭제 -------------------- */
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable Long postId) {
        postCreateService.deletePost(postId);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/user")
    public ResponseEntity<List<PostResponse>> getPostsByUserEmail(@RequestParam String email) {
        List<PostResponse> responses = postCreateService.getPostsByEmail(email);
        return ResponseEntity.ok(responses);
    }
}
