package com.example.Marketten.service;

import com.example.Marketten.domain.Role;
import com.example.Marketten.dto.admin.*;

public interface AdminService {

    /**
     * 사용자 리스트를 페이징 처리하여 조회합니다.
     */
    AdminUserListResponse getUserList(int page, int size, Role role);

    /**
     * 특정 사용자의 권한(Role)을 수정합니다.
     */
    void updateUserRole(Long userId, Role newRole);

    /**
     * 관리자 대시보드에 필요한 통계 데이터를 조회합니다.
     *
     * @return AdminDashboardDTO (전체 사용자 수, 게시글 수 등)
     */
    AdminDashboardDTO getDashboardStats();

    /**
     * 특정 사용자의 상세 정보를 조회합니다.
     *
     * @param userId 조회할 사용자의 고유 ID
     * @return 사용자의 상세 정보 DTO
     */
    AdminUserDetailDTO getUserDetail(Long userId);

    /**
     * 방문자 수를 보여줍니다.
     */
    VisitorStatsDTO getVisitorStats();

    /**
     * 전체 임시 저장 글의 수를 조회합니다.
     */
    long getTempPostCount();

    /**
     * 전체 최종 저장 글의 수를 조회합니다.
     */
    long getFinalPostCount();

    /**
     * 관리자의 비밀번호를 변경합니다.
     *
     * @param adminId           변경할 관리자의 ID
     * @param currentPassword   현재 비밀번호
     * @param newPassword       새 비밀번호
     * @param currentAdminEmail 현재 로그인된 관리자의 이메일 (보안 검증용)
     */
    void updateAdminPassword(Long adminId, String currentPassword, String newPassword, String currentAdminEmail);

    void updateGptModel(String modelName);

    void updateCommonConfig(CommonConfigRequestDTO request);
}