package com.example.Marketten.repository;

import com.example.Marketten.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// JpaRepository를 상속받아 기본적인 CRUD 기능을 사용합니다.
public interface UserRepository extends JpaRepository<User, Long> {

    // 이메일(로그인 ID)로 사용자를 조회하는 메서드 정의
    Optional<User> findByEmail(String email);

    // 이메일 중복 체크를 위한 메서드 정의
    boolean existsByEmail(String email);

    // 닉네임 중복 체크를 위한 메서드 정의
    boolean existsByNickname(String nickname);
}