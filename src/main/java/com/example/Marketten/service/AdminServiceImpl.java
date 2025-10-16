package com.example.Marketten.service;

import com.example.Marketten.domain.Role;
import com.example.Marketten.domain.Status;
import com.example.Marketten.domain.User;
import com.example.Marketten.dto.admin.AdminDashboardDTO;
import com.example.Marketten.dto.admin.AdminUserListResponse;
import com.example.Marketten.dto.admin.AdminUserDetailDTO;
import com.example.Marketten.dto.user.UserResponse;
import com.example.Marketten.repository.FinalPostRepository;
import com.example.Marketten.repository.TempPostRepository;
import com.example.Marketten.repository.UserRepository;
import com.example.Marketten.domain.SiteConfig;
import com.example.Marketten.dto.admin.CommonConfigRequestDTO;
import com.example.Marketten.repository.SiteConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;


import java.util.Map;
import com.example.Marketten.dto.admin.VisitorStatsDTO;
import com.example.Marketten.repository.VisitorLogRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final FinalPostRepository finalPostRepository;
    private final TempPostRepository tempPostRepository;
    private final VisitorLogRepository visitorLogRepository;
    private final SiteConfigRepository siteConfigRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${fastapi.server.url}")
    private String fastapiUrl;

    @Value("${fastapi.server.api-key}")
    private String fastapiApiKey;

    /**
     * 사용자 리스트 조회 로직 (기존과 동일)
     */
    @Override
    @Transactional(readOnly = true)
    // ✨ role 파라미터 추가
    public AdminUserListResponse getUserList(int page, int size, Role role) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        // ✨ findAll 대신 findByRoleAndStatus를 사용하여 필터링
        // (탈퇴하지 않은 ACTIVE 상태의 사용자 중에서 특정 role을 가진 사람만 조회)
        Page<User> userPage = userRepository.findByRoleAndStatus(role, Status.ACTIVE, pageable);

        List<UserResponse> userList = userPage.getContent().stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());

        return AdminUserListResponse.builder()
                .userList(userList)
                .currentPage(userPage.getNumber())
                .totalItems(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .build();
    }

    /**
     * 사용자 권한 수정 로직 (기존과 동일)
     */
    @Override
    public void updateUserRole(Long userId, Role newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: ID " + userId));
        user.setRole(newRole);
        log.info("User role updated. UserID: {}, New Role: {}", userId, newRole);
    }

    /**
     * 관리자 대시보드 통계 데이터 조회 로직 (수정됨)
     */
    @Override
    @Transactional(readOnly = true)
    public AdminDashboardDTO getDashboardStats() {
        // 1. 전체 사용자 수
        long totalUserCount = userRepository.count();

        // 2. 전체 최종글 수
        long totalPostCount = finalPostRepository.count();

        // 3. 전체 임시 저장 글 수
        long tempPostCount = tempPostRepository.count();

        // 4. 오늘 방문자 수 (오늘 00시 이후 로그인한 사용자)
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        long todayVisitorCount = userRepository.countByLastLoginAtAfter(startOfDay);

        // 5. DTO로 빌드하여 반환
        return AdminDashboardDTO.builder()
                .totalUserCount(totalUserCount)
                .totalPostCount(totalPostCount) // 저장 완료 글 수
                .tempPostCount(tempPostCount)   // 임시 저장 글 수
                .todayVisitorCount(todayVisitorCount)
                .build();


    }

    /**
     * 특정 사용자의 상세 정보를 조회하는 로직을 구현합니다.
     */
    @Override
    @Transactional(readOnly = true)
    public AdminUserDetailDTO getUserDetail(Long userId) {
        // 1. ID를 기반으로 User 엔티티를 조회합니다. (없으면 예외 발생)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: ID " + userId));

        // 2. 해당 사용자가 작성한 글의 개수를 각 Repository에서 조회합니다.
        long finalPostCount = finalPostRepository.countByUser(user);
        long tempPostCount = tempPostRepository.countByUser(user);

        // 3. 조회한 모든 정보를 DTO로 변환하여 반환합니다.
        return AdminUserDetailDTO.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .imageUrl(user.getImageUrl())
                .role(user.getRole())
                .provider(user.getProvider().name())   // User 엔티티에 getProvider()가 있다고 가정
                .status(user.getStatus().name())       // User 엔티티에 getStatus()가 있다고 가정
                .createdAt(user.getCreatedAt()) // User 엔티티에 getCreatedAt()이 있다고 가정
                .lastLoginAt(user.getLastLoginAt()) // User 엔티티에 getLastLoginAt()이 있다고 가정
                .totalFinalPosts(finalPostCount)
                .totalTempPosts(tempPostCount)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public VisitorStatsDTO getVisitorStats() {
        LocalDateTime now = LocalDateTime.now();

        // 1. 오늘 방문자 수 (오늘 00:00:00 ~ 현재)
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        long daily = visitorLogRepository.countDistinctVisitorByVisitDateBetween(startOfToday, now);

        // 2. 이번 달 방문자 수 (이번 달 1일 00:00:00 ~ 현재)
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        long monthly = visitorLogRepository.countDistinctVisitorByVisitDateBetween(startOfMonth, now);

        // 3. 올해 방문자 수 (올해 1월 1일 00:00:00 ~ 현재)
        LocalDateTime startOfYear = LocalDate.now().withDayOfYear(1).atStartOfDay();
        long yearly = visitorLogRepository.countDistinctVisitorByVisitDateBetween(startOfYear, now);

        return VisitorStatsDTO.builder()
                .dailyVisitors(daily)
                .monthlyVisitors(monthly)
                .yearlyVisitors(yearly)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public long getTempPostCount() {
        // 이미 구현된 로직을 그대로 재사용합니다.
        return tempPostRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public long getFinalPostCount() {
        // 이미 구현된 로직을 그대로 재사용합니다.
        return finalPostRepository.count();
    }

    @Override
    public void updateAdminPassword(Long adminId, String currentPassword, String newPassword, String currentAdminEmail) {
        // 1. DB에서 변경 대상 관리자 정보를 조회
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("해당 관리자를 찾을 수 없습니다: ID " + adminId));

        // 2. [보안] 현재 로그인한 관리자가 자기 자신인지 확인
        if (!admin.getEmail().equals(currentAdminEmail)) {
            throw new SecurityException("자신의 비밀번호만 변경할 수 있습니다.");
        }

        // 3. [보안] 입력된 현재 비밀번호가 DB에 저장된 비밀번호와 일치하는지 확인
        if (!passwordEncoder.matches(currentPassword, admin.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        // 4. 새 비밀번호를 암호화하여 저장
        admin.setPassword(passwordEncoder.encode(newPassword));
        // @Transactional에 의해 메서드 종료 시 자동으로 DB에 업데이트됩니다. (Dirty Checking)
        log.info("Admin password updated successfully for UserID: {}", adminId);
    }

    @Override
    public void updateGptModel(String modelName) {
        log.info("Sending request to FastAPI server to update model to: {}", modelName);

        // 1. HTTP 요청을 보내기 위한 객체 생성
        RestTemplate restTemplate = new RestTemplate();

        // 2. HTTP 헤더 설정 (Content-Type, API Key)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-API-Key", fastapiApiKey);

        // 3. HTTP 바디 설정 (JSON 형식)
        Map<String, String> requestBody = Map.of("model_name", modelName);

        // 4. 헤더와 바디를 합쳐서 요청 객체 생성
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            // 5. FastAPI 서버에 PUT 요청 보내기
            // 예: http://127.0.0.1:8000/config/model 로 요청
            ResponseEntity<String> response = restTemplate.exchange(
                    fastapiUrl + "/config/model", // 동료와 약속한 경로
                    HttpMethod.PUT,
                    requestEntity,
                    String.class
            );

            // 6. 응답 확인 (선택 사항)
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Successfully updated model on FastAPI server.");
            } else {
                log.error("Failed to update model on FastAPI server. Status: {}", response.getStatusCode());
                throw new RuntimeException("FastAPI 서버 모델 변경에 실패했습니다.");
            }
        } catch (Exception e) {
            log.error("Error while communicating with FastAPI server", e);
            throw new RuntimeException("FastAPI 서버와 통신 중 오류가 발생했습니다.");
        }
    }

    @Override
    public void updateCommonConfig(CommonConfigRequestDTO request) {
        // DTO에 담겨온 각 값을 해당하는 Key를 찾아 DB에 업데이트합니다.
        updateConfigValue("HEADER_TEXT", request.getHeaderText());
        updateConfigValue("FOOTER_TEXT", request.getFooterText());
        updateConfigValue("BANNER_IMAGE_URL", request.getBannerImageUrl());
        log.info("Common site configurations have been updated.");
    }

    /**
     * 특정 설정 키(Key)의 값(Value)을 업데이트하는 헬퍼 메서드
     */
    private void updateConfigValue(String key, String value) {
        // 요청 DTO에 값이 없는(null) 필드는 업데이트하지 않고 건너뜁니다.
        if (value == null) {
            return;
        }

        SiteConfig config = siteConfigRepository.findById(key)
                .orElseThrow(() -> new IllegalArgumentException("설정 키를 찾을 수 없습니다: " + key));

        config.updateValue(value);
        // @Transactional에 의해 메서드 종료 시 자동으로 DB에 저장됩니다.
    }
}