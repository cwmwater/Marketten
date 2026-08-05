package com.example.Marketten.service;

import com.example.Marketten.domain.Role;
import com.example.Marketten.dto.admin.*;

/**
 * 관리자 페이지에서 사용되는 모든 비즈니스 로직을 처리하는 서비스 인터페이스입니다.
 */
public interface AdminService {

    // --- 사용자 관리 ---
    AdminUserListResponse getUserList(int page, int size, Role role);

    AdminUserDetailDTO getUserDetail(Long userId);

    void updateUserRole(Long userId, Role newRole);

    void updateAdminPassword(Long adminId, String currentPassword, String newPassword, String currentAdminEmail);

    // --- 페이지/콘텐츠 관리 ---
    void updateCommonConfig(CommonConfigRequestDTO request);

    void updateGptModel(String modelName);

    // --- 통계 ---
    AdminDashboardStatsDTO getDashboardChartStats();

    long getTempPostCount();

    long getFinalPostCount();

    ReportSummaryDTO getReportSummary();
}
