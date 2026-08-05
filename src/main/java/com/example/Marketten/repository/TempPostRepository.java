package com.example.Marketten.repository;

import com.example.Marketten.domain.TempPost;
import com.example.Marketten.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface TempPostRepository extends JpaRepository<TempPost, Long> {


    long countByUser(User user);

    // 특정 기간 동안 생성/수정된 임시글 수를 계산합니다.
    long countByUpdatedAtBetween(LocalDateTime start, LocalDateTime end);
}