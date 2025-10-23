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


    /**
     * 사용자가 작성한 글 목록을 조회하되, 각 글의 최종 진행 단계(step)를 포함하여 반환합니다.
     */
    @Override
    @Transactional(readOnly = true)
    public List<PostSummaryDTO> getPostsByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        List<FinalPost> finalPosts = finalPostRepository.findByUserOrderByCreatedDateDesc(user);

        return finalPosts.stream().map(post -> {
            // ✨ 1. 글의 상태(status)가 "Complete"이면, 단계(step)는 무조건 4로 확정합니다.
            if ("Complete".equals(post.getStatus())) {
                return PostSummaryDTO.builder()
                        .postId(post.getPostId())
                        .finalTitle(post.getFinalTitle())
                        .step(4) // '완성' 상태는 4단계
                        .status(post.getStatus())
                        .createdDate(post.getCreatedDate())
                        .build();
            }

            // ✨ 2. "Complete"가 아니라면, 연결된 TempPost들 중에서 가장 높은 step 값을 찾습니다.
            Integer currentStep = post.getTempPosts().stream()
                    .map(TempPost::getStep)
                    .max(Integer::compareTo)
                    .orElse(1); // 만약 TempPost가 하나도 없다면, 1단계로 간주

            // ✨ 3. 내비게이션에 필요한 TempPost의 ID도 함께 찾습니다.
            Long tempPostId = post.getTempPosts().stream()
                    .findFirst()
                    .map(TempPost::getInputId)
                    .orElse(null);

            // ✨ 4. 모든 정보를 종합하여 새로운 DTO를 만들어 반환합니다.
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

    private PostResponse toResponse(FinalPost post) {
        Long tempId = null;
        if (post.getTempPosts() != null && !post.getTempPosts().isEmpty()) {
            tempId = post.getTempPosts().get(0).getInputId(); // 첫 번째 임시글 ID
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
