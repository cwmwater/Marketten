package com.example.Marketten.repository;

import com.example.Marketten.domain.FinalPost;
import com.example.Marketten.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import java.time.LocalDateTime;

@Repository
public interface FinalPostRepository extends JpaRepository<FinalPost, Long> {

    // JpaRepository를 상속받는 것만으로도 .count() 같은 기본 메서드는 이미 포함

    /**
     * 특정 상태(status)를 가진 최종글의 수를 계산
     * 예를 들어 "WRITING", "PUBLISHED" 상태의 글 수를 셀 때 사용
     */
    long countByStatus(String status);

    long countByUser(User user);


    List<FinalPost> findByUser(User user);

    // 특정 기간 동안 생성된 최종글 수를 계산합니다.
    long countByCreatedDateBetween(LocalDateTime start, LocalDateTime end);

    List<FinalPost> findByUserOrderByCreatedDateDesc(User user);


}