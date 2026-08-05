package com.example.Marketten.controller;

import com.example.Marketten.domain.Role;
import com.example.Marketten.dto.admin.*;
import com.example.Marketten.security.CustomUserDetails;
import com.example.Marketten.service.AdminService;
import com.example.Marketten.service.CommonService;
import com.example.Marketten.dto.common.CommonConfigResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')") // 이 컨트롤러의 모든 메서드는 'ROLE_ADMIN' 권한이 필요
public class AdminController {

    private final AdminService adminService;
    private final CommonService commonService;

    /**
     * 사용자 리스트 조회 (페이징 처리)
     * 경로: GET /api/admin/users/{page}/{size}
     */
    @GetMapping("/users/{page}/{size}")
    public ResponseEntity<AdminUserListResponse> getUserList(
            @PathVariable int page,
            @PathVariable int size,
            @RequestParam Role role) {

        log.info("Request to get user list: Page={}, Size={}, Role={}", page, size, role);
        AdminUserListResponse response = adminService.getUserList(page, size, role);
        return ResponseEntity.ok(response);
    }

    /**
     * 사용자 상세 정보 조회
     * 경로: GET /api/admin/users/{userid}
     */
    @GetMapping("/users/{userid}")
    public ResponseEntity<AdminUserDetailDTO> getUserDetail(@PathVariable Long userid) {
        log.info("Request to get user detail: UserID={}", userid);
        AdminUserDetailDTO response = adminService.getUserDetail(userid);
        return ResponseEntity.ok(response);
    }

    /**
     * 임시 저장 글의 총 개수 조회
     * 경로: GET /api/admin/tempcount
     */
    @GetMapping("/tempcount")
    public ResponseEntity<AdminCountDTO> getTempPostCount() {
        log.info("Request to get temp post count.");
        long count = adminService.getTempPostCount();
        AdminCountDTO response = new AdminCountDTO(count);
        return ResponseEntity.ok(response);
    }

    /**
     * 최종 저장 글의 총 개수 조회
     * 경로: GET /api/admin/postcount
     */
    @GetMapping("/postcount")
    public ResponseEntity<AdminCountDTO> getFinalPostCount() {
        log.info("Request to get final post count.");
        long count = adminService.getFinalPostCount();
        AdminCountDTO response = new AdminCountDTO(count);
        return ResponseEntity.ok(response);
    }

    /**
     * 사용자 권한 수정
     * 경로: PATCH /api/admin/role/{userid}
     */
    @PatchMapping("/role/{userid}")
    public ResponseEntity<Void> updateUserRole(
            @PathVariable Long userid,
            @RequestBody UserRoleUpdateRequest request) {

        log.info("Request to update role for UserID={}: New Role={}", userid, request.getNewRole());
        adminService.updateUserRole(userid, request.getNewRole());
        return ResponseEntity.ok().build();
    }

    /**
     * 대시보드 차트에 필요한 모든 통계 데이터를 조회합니다.
     * 경로: GET /api/admin
     */
    @GetMapping
    public ResponseEntity<AdminDashboardStatsDTO> getDashboardChartStats() {
        log.info("Request to get dashboard chart statistics.");
        AdminDashboardStatsDTO response = adminService.getDashboardChartStats();
        return ResponseEntity.ok(response);
    }

    // 리포트 요약 데이터 조회 API
    @GetMapping("/visitors")
    public ResponseEntity<ReportSummaryDTO> getReportSummary() {
        ReportSummaryDTO response = adminService.getReportSummary();
        return ResponseEntity.ok(response);
    }

    /**
     * 5. GPT 모델 변경 API
     */
    @PatchMapping("/model")
    public ResponseEntity<Void> updateGPTModel(@RequestBody ModelUpdateRequestDTO request) {
        log.info("Request to update GPT Model to: {}", request.getModelName());
        adminService.updateGptModel(request.getModelName());
        return ResponseEntity.ok().build();
    }


    /**
     * 관리자 자신의 비밀번호 변경
     * 경로: PATCH /api/admin/{adminid}
     */
    @PatchMapping("/{adminid}")
    public ResponseEntity<Void> updateAdminPassword(
            @PathVariable Long adminid,
            @RequestBody AdminPasswordUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        log.info("Request to update password for AdminID: {}", adminid);

        // 서비스 호출 시 현재 로그인된 사용자의 이메일도 함께 넘겨 보안 강화
        adminService.updateAdminPassword(
                adminid,
                request.getCurrentPassword(),
                request.getNewPassword(),
                currentUser.getUsername() // CustomUserDetails에서 이메일(username)을 가져옴
        );

        return ResponseEntity.ok().build();
    }

    /**
     * 사이트 공통 설정 수정
     * 경로: PATCH /api/admin/common
     */
    @PatchMapping("/common")
    public ResponseEntity<Void> updateCommonConfig(@RequestBody CommonConfigRequestDTO request) {
        adminService.updateCommonConfig(request);
        return ResponseEntity.ok().build();
    }

    // 이 외의 API (관리자 비번 수정, 톤 조회/수정 등)는 추후 필요에 따라 구현합니다.
}