package com.example.Marketten.service;

import com.example.Marketten.domain.FinalPost;
import com.example.Marketten.domain.TempPost;
import com.example.Marketten.domain.User;
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


    //보관함 최종글 리스트
    public List<PostResponse> getPostsByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<FinalPost> posts = finalPostRepository.findByUser(user);

        return posts.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
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
