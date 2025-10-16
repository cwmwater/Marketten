package com.example.Marketten.repository;

import com.example.Marketten.domain.Status;
import com.example.Marketten.domain.User;
import com.example.Marketten.domain.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime; // ✨ import 추가
import java.util.Optional;

// JpaRepository를 상속받아 기본적인 CRUD 기능을 사용합니다.
public interface UserRepository extends JpaRepository<User, Long> {


    // ✨ 특정 역할(Role)과 상태(Status)를 가진 사용자만 페이징하여 조회하는 메서드
    Page<User> findByRoleAndStatus(Role role, Status status, Pageable pageable);

    // 이메일(로그인 ID)로 사용자를 조회하는 메서드 정의
    Optional<User> findByEmail(String email);

    // 이메일 중복 체크를 위한 메서드 정의
    boolean existsByEmail(String email);

    // 닉네임 중복 체크를 위한 메서드 정의
    boolean existsByNickname(String nickname);

    /**
     * 특정 시간 이후에 마지막으로 로그인한 사용자 수를 계산합니다.
     *
     * @param dateTime 기준 시간
     * @return 사용자 수
     */
    long countByLastLoginAtAfter(LocalDateTime dateTime);

    long countByStatus(Status status);
}