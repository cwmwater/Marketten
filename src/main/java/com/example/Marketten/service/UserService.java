package com.example.Marketten.service;

import com.example.Marketten.dto.user.UserPasswordUpdateRequest;
import com.example.Marketten.dto.user.UserResponse;
import com.example.Marketten.dto.user.UserUpdateRequest;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {

    /**
     * 사용자 정보를 조회합니다.
     *
     * @param email 조회할 사용자의 이메일
     * @return UserResponse DTO
     */
    UserResponse getUserInfo(String email);

    /**
     * 사용자 정보를 수정합니다. (닉네임, 이미지 파일 포함)
     *
     * @param email        수정할 사용자의 이메일
     * @param request      수정 요청 DTO (닉네임 등 텍스트 정보)
     * @param profileImage 업로드된 프로필 이미지 파일 (선택 사항)
     * @return UserResponse DTO (수정된 사용자 정보)
     */
    UserResponse updateUserInfo(String email, UserUpdateRequest request, MultipartFile profileImage);

    /**
     * 비밀번호를 수정합니다. (기존 비밀번호 확인 필수)
     *
     * @param email   비밀번호를 수정할 사용자의 이메일
     * @param request 비밀번호 변경 요청 DTO
     */
    void updatePassword(String email, UserPasswordUpdateRequest request);

    /**
     * 회원을 탈퇴시킵니다. (사용자 데이터 및 Refresh Token 삭제)
     *
     * @param email 탈퇴할 사용자의 이메일
     */
    void withdrawUser(String email);


    /**
     * 이메일 기반 비밀번호 재설정 (기존 비밀번호 확인 없이 새 비밀번호 설정)
     *
     * @param email   비밀번호를 재설정할 사용자의 이메일
     * @param newPassword 새 비밀번호
     */
    public void resetPasswordByEmail(String email, String newPassword);
}