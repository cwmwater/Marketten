package com.example.Marketten.service;

import com.example.Marketten.dto.post.PostRequest;
import com.example.Marketten.dto.post.PostResponse;
import com.example.Marketten.dto.post.PostSummaryDTO;
import com.example.Marketten.dto.post.PostUpdateRequest;

import java.util.List;

public interface PostCreateService {


    // 게시글 수정: 기존 게시글(postId)을 요청 데이터로 덮어쓰고 수정된 정보를 반환
    PostResponse updatePost(Long postId, PostUpdateRequest request);

    // 게시글 삭제: postId에 해당하는 게시글을 DB에서 삭제
    void deletePost(Long postId);

    // 게시글 조회: postId에 해당하는 게시글 정보를 반환
    PostResponse getPost(Long postId);

    // 보관함 글 리스트 조회
    List<PostSummaryDTO> getPostsByEmail(String email);

}
