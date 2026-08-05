package com.example.Marketten.service;

import com.example.Marketten.dto.user.MyPageUserResponse;
import com.example.Marketten.dto.user.PasswordInitRequest;
import com.example.Marketten.dto.user.UserPasswordUpdateRequest;
import com.example.Marketten.dto.user.UserUpdateRequest;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {

    /**
     * 현재 로그인된 사용자의 정보(마이페이지용)를 조회합니다.
     *
     * @param username 현재 인증된 사용자의 이메일
     * @return MyPageUserResponse DTO
     */
    MyPageUserResponse getMyInfo(String username);

    /**
     * 현재 로그인된 사용자의 정보(닉네임, 프로필 이미지)를 수정합니다.
     *
     * @param username     현재 인증된 사용자의 이메일
     * @param request      수정 요청 DTO (닉네임 등 텍스트 정보)
     * @param profileImage 업로드된 프로필 이미지 파일 (선택 사항)
     */
    void updateMyInfo(String username, UserUpdateRequest request, MultipartFile profileImage);

    /**
     * 현재 로그인된 사용자의 비밀번호를 변경합니다.
     *
     * @param username 현재 인증된 사용자의 이메일
     * @param request  비밀번호 변경 요청 DTO
     */
    void updateMyPassword(String username, UserPasswordUpdateRequest request);

    /**
     * 현재 로그인된 사용자(소셜)의 비밀번호를 초기 설정합니다.
     *
     * @param username 현재 인증된 사용자의 이메일
     * @param request  비밀번호 초기 설정 DTO
     */
    void initMyPassword(String username, PasswordInitRequest request);

    /**
     * 현재 로그인된 사용자를 탈퇴 처리합니다.
     *
     * @param username 현재 인증된 사용자의 이메일
     */
    void withdrawMe(String username);


    /**
     * 이메일 기반 비밀번호 재설정 (기존 비밀번호 확인 없이 새 비밀번호 설정)
     *
     * @param email       비밀번호를 재설정할 사용자의 이메일
     * @param newPassword 새 비밀번호
     */
    void resetPasswordByEmail(String email, String newPassword);

    void completeTutorial(String username);
}