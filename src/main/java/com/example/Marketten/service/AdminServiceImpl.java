package com.example.Marketten.service;

import com.example.Marketten.domain.Role;
import com.example.Marketten.domain.User;
import com.example.Marketten.dto.admin.AdminDashboardDTO;
import com.example.Marketten.dto.admin.AdminUserListResponse;
import com.example.Marketten.dto.admin.AdminUserDetailDTO;
import com.example.Marketten.dto.user.UserResponse;
import com.example.Marketten.repository.FinalPostRepository;
import com.example.Marketten.repository.TempPostRepository;
import com.example.Marketten.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

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
    private final PasswordEncoder passwordEncoder;

    /**
     * 사용자 리스트 조회 로직 (기존과 동일)
     */
    @Override
    @Transactional(readOnly = true)
    public AdminUserListResponse getUserList(int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<User> userPage = userRepository.findAll(pageable);
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
}