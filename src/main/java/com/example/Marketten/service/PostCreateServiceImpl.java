package com.example.Marketten.service;

import com.example.Marketten.domain.FinalPost;
import com.example.Marketten.domain.TempPost;
import com.example.Marketten.domain.User;
import com.example.Marketten.dto.post.PostSummaryDTO;
import com.example.Marketten.dto.post.PostRequest;
import com.example.Marketten.dto.post.PostResponse;
import com.example.Marketten.dto.post.PostUpdateRequest;
import com.example.Marketten.repository.FinalPostRepository;
import com.example.Marketten.repository.TempPostRepository;
import com.example.Marketten.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PostCreateServiceImpl implements PostCreateService {

    private final FinalPostRepository finalPostRepository;
    private final UserRepository userRepository;

    // 최종글 수정
    @Override
    public PostResponse updatePost(Long postId, PostUpdateRequest request) {
        FinalPost post = finalPostRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("FinalPost not found"));

        // 전달된 필드만 수정
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

    // 최종글 삭제
    @Override
    public void deletePost(Long postId) {
        FinalPost post = finalPostRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("FinalPost not found"));
        finalPostRepository.delete(post);
    }

    // 최종글 단건 조회
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

    // 사용자가 작성한 글 목록 조회
    @Override
    @Transactional(readOnly = true)
    public List<PostSummaryDTO> getPostsByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        List<FinalPost> finalPosts = finalPostRepository.findByUserOrderByCreatedDateDesc(user);

        return finalPosts.stream().map(post -> {
            // 상태가 Complete이면 단계는 4로 고정
            if ("Complete".equals(post.getStatus())) {
                return PostSummaryDTO.builder()
                        .postId(post.getPostId())
                        .finalTitle(post.getFinalTitle())
                        .step(4)
                        .status(post.getStatus())
                        .createdDate(post.getCreatedDate())
                        .build();
            }

            // Complete가 아니면 TempPost 중 가장 높은 step을 찾음
            Integer currentStep = post.getTempPosts().stream()
                    .map(TempPost::getStep)
                    .max(Integer::compareTo)
                    .orElse(1);

            // TempPost 중 하나의 ID를 가져옴
            Long tempPostId = post.getTempPosts().stream()
                    .findFirst()
                    .map(TempPost::getInputId)
                    .orElse(null);

            // 요약 DTO 생성
            return PostSummaryDTO.builder()
                    .postId(post.getPostId())
                    .tempPostId(tempPostId)
                    .finalTitle(post.getFinalTitle())
                    .step(currentStep)
                    .status(post.getStatus())
                    .createdDate(post.getCreatedDate())
                    .build();
        }).collect(Collectors.toList());
    }

    // FinalPost를 PostResponse로 변환
    private PostResponse toResponse(FinalPost post) {
        Long tempId = null;
        if (post.getTempPosts() != null && !post.getTempPosts().isEmpty()) {
            tempId = post.getTempPosts().get(0).getInputId();
        }

        return PostResponse.builder()
                .postId(post.getPostId())
                .tempPostId(tempId)
                .finalTitle(post.getFinalTitle())
                .finalContent(post.getFinalContent())
                .finalTone(post.getFinalTone())
                .status(post.getStatus())
                .createdDate(post.getCreatedDate())
                .updatedDate(post.getUpdatedDate())
                .build();
    }
}
