package com.example.Marketten.repository;

import com.example.Marketten.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {
    // 이메일로 회원 조회 (이메일 통합 로그인용)
    Optional<User> findByEmail(String email);
}
