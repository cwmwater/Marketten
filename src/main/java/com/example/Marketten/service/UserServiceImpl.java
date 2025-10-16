package com.example.Marketten.service;

import com.example.Marketten.domain.Status;
import com.example.Marketten.domain.User;
import com.example.Marketten.domain.SocialProvider;
import com.example.Marketten.dto.user.MyPageUserResponse;
import com.example.Marketten.dto.user.PasswordInitRequest;
import com.example.Marketten.dto.user.UserPasswordUpdateRequest;
import com.example.Marketten.dto.user.UserUpdateRequest;
import com.example.Marketten.repository.FinalPostRepository;
import com.example.Marketten.repository.RefreshTokenRepository;
import com.example.Marketten.repository.TempPostRepository;
import com.example.Marketten.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final FileUploadService fileUploadService;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final FinalPostRepository finalPostRepository;
    private final TempPostRepository tempPostRepository;

    @Value("${app.base-url}")
    private String baseUrl;

    /**
     * 현재 로그인된 사용자의 정보(마이페이지용)를 조회합니다.
     */
    @Override
    @Transactional(readOnly = true)
    public MyPageUserResponse getMyInfo(String username) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + username));

        long finalPostCount = finalPostRepository.countByUser(user);
        long tempPostCount = tempPostRepository.countByUser(user);

        String fullImageUrl = user.getImageUrl();
        if (fullImageUrl != null && fullImageUrl.startsWith("/")) {
            fullImageUrl = baseUrl + fullImageUrl;
        }

        // ✨ 사용자의 비밀번호 필드가 null이 아니면 true, null이면 false가 됩니다.
        boolean passwordExists = user.getPassword() != null;

        return MyPageUserResponse.builder()
                .role(user.getRole())
                .nickname(user.getNickname())
                .imageUrl(fullImageUrl)
                .provider(user.getProvider().name())
                .createdAt(user.getCreatedAt().toString())
                .lastLoginAt(user.getLastLoginAt() != null ? user.getLastLoginAt().toString() : null)
                .totalFinalPosts(finalPostCount)
                .totalTempPosts(tempPostCount)
                .passwordExists(passwordExists) // ✨ 빌더에 값을 채워서 보냅니다.
                .build();
    }

    /**
     * 현재 로그인된 사용자의 정보(닉네임, 프로필 이미지)를 수정합니다.
     */
    @Override
    public void updateMyInfo(String username, UserUpdateRequest request, MultipartFile profileImage) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + username));

        if (request != null && request.getNickname() != null) {
            if (!user.getNickname().equals(request.getNickname()) &&
                    userRepository.existsByNickname(request.getNickname())) {
                throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
            }
            user.setNickname(request.getNickname());
        }

        if (profileImage != null && !profileImage.isEmpty()) {
            String uploadedUrl = fileUploadService.uploadProfileImage(profileImage);
            user.setImageUrl(uploadedUrl);
        }   
    }

    /**
     * 현재 로그인된 사용자의 비밀번호를 변경합니다.
     */
    @Override
    public void updateMyPassword(String username, UserPasswordUpdateRequest request) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + username));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        log.info("Password updated successfully for user: {}", username);
    }

    /**
     * 현재 로그인된 사용자(소셜)의 비밀번호를 초기 설정합니다.
     */
    @Override
    public void initMyPassword(String username, PasswordInitRequest request) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + username));

        if (user.getProvider() == SocialProvider.SITE) {
            throw new IllegalStateException("일반 가입자는 이 기능을 사용할 수 없습니다. '비밀번호 변경'을 이용해주세요.");
        }
        if (user.getPassword() != null) {
            throw new IllegalStateException("이미 비밀번호가 설정되어 있습니다.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        log.info("Password initialized successfully for social user: {}", username);
    }

    /**
     * 현재 로그인된 사용자를 탈퇴 처리합니다. (논리적 삭제)
     */
    @Override
    public void withdrawMe(String username) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + username));

        refreshTokenRepository.findById(username).ifPresent(refreshTokenRepository::delete);
        log.info("Redis Refresh Token deleted for user: {}", username);

        user.setStatus(Status.DEACTIVATED);
        log.info("User status set to DEACTIVATED for: {}", username);
    }


    /**
     * 이메일 기반 비밀번호 재설정 (관리자용 또는 비밀번호 찾기 기능)
     * 인터페이스에 선언된 메서드를 구현합니다.
     */
    @Override
    public void resetPasswordByEmail(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + email));

        // 새 비밀번호를 암호화하여 저장
        user.setPassword(passwordEncoder.encode(newPassword));

        log.info("Password has been reset for user: {}", email);
    }
}