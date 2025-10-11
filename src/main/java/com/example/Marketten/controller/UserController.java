package com.example.Marketten.controller;

import com.example.Marketten.dto.user.UserPasswordUpdateRequest;
import com.example.Marketten.dto.user.UserResponse;
import com.example.Marketten.dto.user.UserUpdateRequest;
import com.example.Marketten.service.UserService;
import com.example.Marketten.util.JWTUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final JWTUtil jwtUtil;

    /**
     * 헬퍼 메서드: 요청 헤더에서 Access Token을 추출하고 이메일을 파싱합니다.
     */
    private String extractEmailFromToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("인증 정보(Access Token)가 누락되었습니다.");
        }
        String accessToken = authHeader.substring(7);
        return jwtUtil.parseEmailFromToken(accessToken);
    }


    /**
     * 1. 내 정보 조회 (Access Token 필요)
     * 경로: GET /api/users/{email}
     */
    @GetMapping("/{email}")
    public ResponseEntity<UserResponse> getUserInfo(
            @PathVariable String email,
            HttpServletRequest request) {

        // 1. 토큰에서 이메일 추출 및 유효성 검증
        String authenticatedEmail = extractEmailFromToken(request);

        // 2. 중요: 요청 경로의 {email}과 토큰에서 추출된 이메일이 일치하는지 확인 (자신의 정보만 조회 가능)
        if (!email.equals(authenticatedEmail)) {
            throw new IllegalArgumentException("요청 경로의 사용자와 인증된 사용자가 일치하지 않습니다.");
        }

        // 3. 서비스 로직 호출
        UserResponse response = userService.getUserInfo(authenticatedEmail);
        return ResponseEntity.ok(response);
    }

    /**
     * 2. 내 정보 수정 (닉네임, 이미지 URL 포함 - Multipart 요청)
     * 경로: PATCH /api/users/{email}
     * consumes 설정을 제거하여 경로 충돌을 방지합니다.
     */
    @PatchMapping(value = "/{email}")
    public ResponseEntity<UserResponse> updateUserInfo(
            @PathVariable String email,
            @RequestPart(name = "request") @Valid UserUpdateRequest request,
            @RequestPart(name = "profileImage", required = false) MultipartFile profileImage,
            HttpServletRequest httpServletRequest) {

        // 1. 토큰에서 이메일 추출 및 유효성 검증
        String authenticatedEmail = extractEmailFromToken(httpServletRequest);

        // 2. 중요: 요청 경로의 {email}과 토큰에서 추출된 이메일이 일치하는지 확인
        if (!email.equals(authenticatedEmail)) {
            throw new IllegalArgumentException("요청 경로의 사용자와 인증된 사용자가 일치하지 않습니다.");
        }

        // 3. 서비스 로직 호출
        UserResponse response = userService.updateUserInfo(authenticatedEmail, request, profileImage);
        return ResponseEntity.ok(response);
    }

    /**
     * 3. 비밀번호를 수정합니다.
     * 경로: PATCH /api/users/{email}/pw
     */
    @PatchMapping(value = "/{email}/pw", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> updatePassword(
            @PathVariable String email,
            @RequestBody @Valid UserPasswordUpdateRequest request) {

        // Note: URL 경로 이메일이 토큰과 일치하는지는 서비스 로직이나 필터에서 처리한다고 가정
        userService.updatePassword(email, request);
        return ResponseEntity.ok().build();
    }

    /**
     * 4. 회원을 탈퇴시킵니다.
     * 경로: DELETE /api/users/{email}
     */
    @DeleteMapping("/{email}") // ✨ DELETE 엔드포인트 추가
    public ResponseEntity<Void> withdrawUser(
            @PathVariable String email,
            HttpServletRequest request) {

        // 1. 토큰에서 이메일 추출 및 유효성 검증 (인증)
        String authenticatedEmail = extractEmailFromToken(request);

        // 2. 중요: 요청 경로의 {email}과 토큰에서 추출된 이메일이 일치하는지 확인
        if (!email.equals(authenticatedEmail)) {
            throw new IllegalArgumentException("요청 경로의 사용자와 인증된 사용자가 일치하지 않습니다.");
        }

        // 3. 서비스 로직 호출 (Redis 토큰 및 DB 데이터 삭제)
        userService.withdrawUser(authenticatedEmail);

        // 성공 시 200 OK 반환
        return ResponseEntity.ok().build();
    }
}