package com.example.Marketten.service;

import com.example.Marketten.domain.FinalPost;
import com.example.Marketten.domain.TempPost;
import com.example.Marketten.dto.post.PostRequest;
import com.example.Marketten.dto.post.PostResponse;
import com.example.Marketten.dto.post.PostUpdateRequest;
import com.example.Marketten.repository.FinalPostRepository;
import com.example.Marketten.repository.TempPostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PostCreateServiceImpl implements PostCreateService {

    private final FinalPostRepository finalPostRepository;

    /** -------------------- 최종글 수정 -------------------- */
    @Override
    public PostResponse updatePost(Long postId, PostUpdateRequest request) {
        FinalPost post = finalPostRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("FinalPost not found"));

        // 최종글 필드만 수정
        if (request.getFinalTitle() != null) post.setFinalTitle(request.getFinalTitle());
        if (request.getFinalContent() != null) post.setFinalContent(request.getFinalContent());
        if (request.getFinalTone() != null) post.setFinalTone(request.getFinalTone());
        if (request.getStatus() != null) post.setStatus(request.getStatus());

        post.setUpdatedDate(LocalDateTime.now());
        finalPostRepository.save(post);

        return PostResponse.builder()
                .postId(post.getPostId())
                .finalTitle(post.getFinalTitle())
                .finalContent(post.getFinalContent())
                .finalTone(post.getFinalTone())
                .status(post.getStatus())
                .createdDate(post.getCreatedDate())
                .updatedDate(post.getUpdatedDate())
                .build();
    }

    /** -------------------- 최종글 삭제 -------------------- */
    @Override
    public void deletePost(Long postId) {
        FinalPost post = finalPostRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("FinalPost not found"));
        finalPostRepository.delete(post);
    }

    /** -------------------- 최종글 조회 -------------------- */
    @Override
    public PostResponse getPost(Long postId) {
        FinalPost post = finalPostRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("FinalPost not found"));

        return PostResponse.builder()
                .postId(post.getPostId())
                .finalTitle(post.getFinalTitle())
                .finalContent(post.getFinalContent())
                .finalTone(post.getFinalTone())
                .status(post.getStatus())
                .createdDate(post.getCreatedDate())
                .updatedDate(post.getUpdatedDate())
                .build();
    }
}
