package com.example.Marketten.controller;

import com.example.Marketten.domain.User;
import com.example.Marketten.dto.user.MyPageUserResponse; // ✨ 내 정보 조회를 위한 별도 DTO (아래에서 생성)
import com.example.Marketten.dto.user.PasswordInitRequest; // ✨ 비밀번호 초기화 DTO (아래에서 생성)
import com.example.Marketten.dto.user.UserPasswordUpdateRequest;
import com.example.Marketten.dto.user.UserUpdateRequest;
import com.example.Marketten.repository.UserRepository;
import com.example.Marketten.security.CustomUserDetails; // ✨ CustomUserDetails 임포트
import com.example.Marketten.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal; // ✨ AuthenticationPrincipal 임포트
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users") // 기본 경로는 그대로 /api/users
public class UserController {

    private final UserService userService;

    /**
     * 1. 내 정보 조회 API
     * 경로: GET /api/users/me
     * JWT 토큰을 기반으로 "나"의 정보를 조회합니다.
     */
    @GetMapping("/me")
    public ResponseEntity<MyPageUserResponse> getMyInfo(
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        String currentUsername = currentUser.getUsername(); // 이메일 가져오기
        MyPageUserResponse response = userService.getMyInfo(currentUsername);
        return ResponseEntity.ok(response);
    }

    /**
     * 2. 내 정보 수정 API (닉네임, 프로필 이미지)
     * 경로: PATCH /api/users/me
     * JWT 토큰을 기반으로 "나"의 정보를 수정합니다.
     */
    @PatchMapping(value = "/me", consumes = {"multipart/form-data"})
    public ResponseEntity<Void> updateMyInfo(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestPart(name = "request", required = false) @Valid UserUpdateRequest request,
            @RequestPart(name = "profileImage", required = false) MultipartFile profileImage) {

        String currentUsername = currentUser.getUsername();
        userService.updateMyInfo(currentUsername, request, profileImage);
        return ResponseEntity.ok().build();
    }

    /**
     * 3. 비밀번호 변경 API
     * 경로: PATCH /api/users/me/password
     */
    @PatchMapping("/me/password")
    public ResponseEntity<Void> updateMyPassword(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestBody @Valid UserPasswordUpdateRequest request) {

        userService.updateMyPassword(currentUser.getUsername(), request);
        return ResponseEntity.ok().build();
    }

    /**
     * 4. 비밀번호 초기 설정 API (소셜용)
     * 경로: POST /api/users/me/init-password
     */
    @PostMapping("/me/init-password")
    public ResponseEntity<Void> initMyPassword(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestBody @Valid PasswordInitRequest request) {

        userService.initMyPassword(currentUser.getUsername(), request);
        return ResponseEntity.ok().build();
    }

    /**
     * 5. 회원 탈퇴 API
     * 경로: DELETE /api/users/me
     */
    @DeleteMapping("/me")
    public ResponseEntity<Void> withdrawMe(
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        userService.withdrawMe(currentUser.getUsername());
        return ResponseEntity.ok().build();
    }

    /**
     * 사용자의 온보딩 튜토리얼 완료 상태를 기록합니다.
     * 경로: PATCH /api/users/me/tutorial-complete
     */
    @PatchMapping("/me/tutorial-complete")
    public ResponseEntity<Void> completeTutorial(@AuthenticationPrincipal CustomUserDetails currentUser) {
        userService.completeTutorial(currentUser.getUsername());
        return ResponseEntity.ok().build();
    }

}