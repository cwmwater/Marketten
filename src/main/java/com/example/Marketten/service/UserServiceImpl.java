package com.example.Marketten.service;

import com.example.Marketten.domain.RefreshToken;
import com.example.Marketten.domain.User;
import com.example.Marketten.dto.user.UserPasswordUpdateRequest;
import com.example.Marketten.dto.user.UserResponse;
import com.example.Marketten.dto.user.UserUpdateRequest;
import com.example.Marketten.repository.RefreshTokenRepository; // ✨ Redis Repository 임포트 추가
import com.example.Marketten.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional; // Optional 임포트 추가

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final FileUploadService fileUploadService;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository; // ✨ Redis Repository 주입

    /**
     * 사용자 정보를 조회합니다.
     *
     * @param email 조회할 사용자의 이메일
     * @return UserResponse DTO (사용자 정보)
     */
    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserInfo(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + email));

        return UserResponse.from(user);
    }

    /**
     * 사용자 정보를 수정합니다. (닉네임, 이미지 URL)
     *
     * @param email        수정할 사용자의 이메일
     * @param request      수정 요청 DTO
     * @param profileImage 업로드된 프로필 이미지 파일
     * @return UserResponse DTO (수정된 사용자 정보)
     */
    @Override
    public UserResponse updateUserInfo(String email, UserUpdateRequest request, MultipartFile profileImage) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + email));

        // 1. 닉네임 중복 검사
        if (!user.getNickname().equals(request.getNickname()) &&
                userRepository.existsByNickname(request.getNickname())) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        // 2. 파일 업로드 처리
        if (profileImage != null && !profileImage.isEmpty()) {
            String uploadedUrl = fileUploadService.uploadProfileImage(profileImage);
            user.setImageUrl(uploadedUrl); // 이미지 URL 업데이트
        } else {
            user.setImageUrl(request.getImageUrl());
        }

        // 3. 닉네임 업데이트
        user.setNickname(request.getNickname());

        return UserResponse.from(user);
    }

    /**
     * 비밀번호를 수정합니다.
     *
     * @param email   비밀번호를 수정할 사용자의 이메일
     * @param request 비밀번호 변경 요청 DTO
     */
    @Override
    public void updatePassword(String email, UserPasswordUpdateRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + email));

        // 1. 기존 비밀번호 검증
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        // 2. 새 비밀번호 암호화 및 저장
        String newEncodedPassword = passwordEncoder.encode(request.getNewPassword());
        user.setPassword(newEncodedPassword); // User 엔티티에 setPassword 메서드가 있다고 가정
    }

    /**
     * 회원을 탈퇴시킵니다. (사용자 데이터 및 Refresh Token 삭제)
     *
     * @param email 탈퇴할 사용자의 이메일
     */
    @Override
    public void withdrawUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + email));

        // 1. Redis에서 Refresh Token 삭제 (로그아웃 처리)
        // Refresh Token의 ID는 email과 동일하므로 findById 사용
        Optional<RefreshToken> tokenOptional = refreshTokenRepository.findById(email);
        tokenOptional.ifPresent(refreshTokenRepository::delete);
        log.info("Redis Refresh Token deleted for user: {}", email);

        // 2. DB에서 사용자 데이터 삭제
        userRepository.delete(user);
        log.info("User successfully withdrawn: {}", email);
    }


    /**
     * 이메일 기반 비밀번호 재설정 (기존 비밀번호 확인 없이 새 비밀번호 설정)
     *
     * @param email   비밀번호를 재설정할 사용자의 이메일
     * @param newPassword 새 비밀번호
     */
    @Override
    public void resetPasswordByEmail(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + email));

        // 새 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedPassword);

        log.info("Password reset successfully for user: {}", email);
    }
}