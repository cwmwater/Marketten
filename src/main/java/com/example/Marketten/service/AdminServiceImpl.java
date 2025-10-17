package com.example.Marketten.service;

import com.example.Marketten.domain.*;
import com.example.Marketten.dto.admin.*;
import com.example.Marketten.dto.user.UserResponse;
import com.example.Marketten.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AdminServiceImpl implements AdminService {

    // --- 의존성 주입 ---
    private final UserRepository userRepository;
    private final VisitorLogRepository visitorLogRepository;
    private final FinalPostRepository finalPostRepository;
    private final TempPostRepository tempPostRepository;
    private final SiteConfigRepository siteConfigRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${fastapi.server.url}")
    private String fastapiUrl;
    @Value("${fastapi.server.api-key}")
    private String fastapiApiKey;
    @Value("${app.base-url}")
    private String baseUrl;

    @Override
    @Transactional(readOnly = true)
    public AdminUserListResponse getUserList(int page, int size, Role role) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
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

    @Override
    @Transactional(readOnly = true)
    public AdminUserDetailDTO getUserDetail(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: ID " + userId));
        long finalPostCount = finalPostRepository.countByUser(user);
        long tempPostCount = tempPostRepository.countByUser(user);
        String fullImageUrl = user.getImageUrl();
        if (fullImageUrl != null && fullImageUrl.startsWith("/")) {
            fullImageUrl = baseUrl + fullImageUrl;
        }
        return AdminUserDetailDTO.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .imageUrl(fullImageUrl)
                .role(user.getRole())
                .provider(user.getProvider().name())
                .status(user.getStatus().name())
                .createdAt(user.getCreatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .totalFinalPosts(finalPostCount)
                .totalTempPosts(tempPostCount)
                .build();
    }

    @Override
    public void updateUserRole(Long userId, Role newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: ID " + userId));
        user.setRole(newRole);
    }

    @Override
    public void updateAdminPassword(Long adminId, String currentPassword, String newPassword, String currentAdminEmail) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("해당 관리자를 찾을 수 없습니다: ID " + adminId));
        if (!admin.getEmail().equals(currentAdminEmail)) {
            throw new SecurityException("자신의 비밀번호만 변경할 수 있습니다.");
        }
        if (!passwordEncoder.matches(currentPassword, admin.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }
        admin.setPassword(passwordEncoder.encode(newPassword));
    }

    @Override
    public void updateCommonConfig(CommonConfigRequestDTO request) {
        updateConfigValue("MAIN_TITLE", request.getMainTitle());
        updateConfigValue("MAIN_SUBTITLE", request.getMainSubtitle());
        updateConfigValue("CTA_TITLE", request.getCallToActionTitle());
        updateConfigValue("FOOTER_COMPANY_NAME", request.getFooterCompanyName());
        updateConfigValue("FOOTER_ADDRESS", request.getFooterAddress());
        updateConfigValue("FOOTER_EMAIL", request.getFooterEmail());
        updateConfigValue("FOOTER_COPYRIGHT", request.getFooterCopyright());
    }

    private void updateConfigValue(String key, String value) {
        if (value == null) return;
        SiteConfig config = siteConfigRepository.findById(key)
                .orElseThrow(() -> new IllegalArgumentException("설정 키를 찾을 수 없습니다: " + key));
        config.updateValue(value);
    }

    @Override
    public void updateGptModel(String modelName) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-API-Key", fastapiApiKey);
        Map<String, String> requestBody = Map.of("model_name", modelName);
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);
        try {
            restTemplate.exchange(fastapiUrl + "/config/model", HttpMethod.PUT, requestEntity, String.class);
        } catch (Exception e) {
            log.error("Error while communicating with FastAPI server", e);
            throw new RuntimeException("FastAPI 서버와 통신 중 오류가 발생했습니다.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AdminDashboardStatsDTO getDashboardChartStats() {
        LocalDateTime now = LocalDateTime.now();
        return AdminDashboardStatsDTO.builder()
                .daily(getStatsForLast7Days(now))
                .weekly(getStatsForLast4Weeks(now))
                .monthly(getStatsForLast12Months(now))
                .yearly(getStatsForLast4Years(now))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public long getTempPostCount() {
        return tempPostRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public long getFinalPostCount() {
        return finalPostRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public ReportSummaryDTO getReportSummary() {
        return ReportSummaryDTO.builder()
                .totalUniqueVisitors(visitorLogRepository.countDistinctVisitor())
                .activeUsers(userRepository.countByStatus(Status.ACTIVE))
                .totalFinalPosts(finalPostRepository.count())
                .build();
    }

    // --- 통계 계산을 위한 헬퍼(Helper) 메서드들 ---
    private PeriodStatsDTO getStatsForLast7Days(LocalDateTime now) {
        List<ChartPointDTO> visitors = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDateTime day = now.minusDays(i);
            long count = visitorLogRepository.countDistinctVisitorByVisitDateBetween(day.with(LocalTime.MIN), day.with(LocalTime.MAX));
            visitors.add(new ChartPointDTO(day.format(DateTimeFormatter.ofPattern("MM-dd")), count));
        }
        Collections.reverse(visitors);
        List<ChartPointDTO> posts = List.of(
                new ChartPointDTO("임시저장글", tempPostRepository.countByUpdatedAtBetween(now.minusDays(1), now)),
                new ChartPointDTO("최종글", finalPostRepository.countByCreatedDateBetween(now.minusDays(1), now))
        );
        return PeriodStatsDTO.builder().visitorStats(visitors).postStats(posts).build();
    }

    private PeriodStatsDTO getStatsForLast4Weeks(LocalDateTime now) {
        List<ChartPointDTO> visitors = new ArrayList<>();
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        for (int i = 0; i < 4; i++) {
            LocalDateTime week = now.minusWeeks(i);
            LocalDateTime start = week.with(DayOfWeek.MONDAY).with(LocalTime.MIN);
            LocalDateTime end = week.with(DayOfWeek.SUNDAY).with(LocalTime.MAX);
            long count = visitorLogRepository.countDistinctVisitorByVisitDateBetween(start, end);
            visitors.add(new ChartPointDTO(week.get(weekFields.weekOfWeekBasedYear()) + "주차", count));
        }
        Collections.reverse(visitors);
        List<ChartPointDTO> posts = List.of(
                new ChartPointDTO("임시저장글", tempPostRepository.countByUpdatedAtBetween(now.minusWeeks(1), now)),
                new ChartPointDTO("최종글", finalPostRepository.countByCreatedDateBetween(now.minusWeeks(1), now))
        );
        return PeriodStatsDTO.builder().visitorStats(visitors).postStats(posts).build();
    }

    private PeriodStatsDTO getStatsForLast12Months(LocalDateTime now) {
        List<ChartPointDTO> visitors = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            LocalDateTime month = now.minusMonths(i);
            LocalDateTime start = month.withDayOfMonth(1).with(LocalTime.MIN);
            LocalDateTime end = month.with(TemporalAdjusters.lastDayOfMonth()).with(LocalTime.MAX);
            long count = visitorLogRepository.countDistinctVisitorByVisitDateBetween(start, end);
            visitors.add(new ChartPointDTO(start.format(DateTimeFormatter.ofPattern("yy-MM")), count));
        }
        Collections.reverse(visitors);
        List<ChartPointDTO> posts = List.of(
                new ChartPointDTO("임시저장글", tempPostRepository.countByUpdatedAtBetween(now.minusMonths(1), now)),
                new ChartPointDTO("최종글", finalPostRepository.countByCreatedDateBetween(now.minusMonths(1), now))
        );
        return PeriodStatsDTO.builder().visitorStats(visitors).postStats(posts).build();
    }

    private PeriodStatsDTO getStatsForLast4Years(LocalDateTime now) {
        List<ChartPointDTO> visitors = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            LocalDateTime year = now.minusYears(i);
            LocalDateTime start = year.withDayOfYear(1).with(LocalTime.MIN);
            LocalDateTime end = year.with(TemporalAdjusters.lastDayOfYear()).with(LocalTime.MAX);
            long count = visitorLogRepository.countDistinctVisitorByVisitDateBetween(start, end);
            visitors.add(new ChartPointDTO(String.valueOf(start.getYear()), count));
        }
        Collections.reverse(visitors);
        List<ChartPointDTO> posts = List.of(
                new ChartPointDTO("임시저장글", tempPostRepository.countByUpdatedAtBetween(now.minusYears(1), now)),
                new ChartPointDTO("최종글", finalPostRepository.countByCreatedDateBetween(now.minusYears(1), now))
        );
        return PeriodStatsDTO.builder().visitorStats(visitors).postStats(posts).build();
    }
}
