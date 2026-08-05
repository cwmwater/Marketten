package com.example.Marketten.repository;

import com.example.Marketten.domain.VisitorLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface VisitorLogRepository extends JpaRepository<VisitorLog, Long> {

    /**
     * 특정 기간 동안의 순 방문자 수 (중복 제거된 사용자 수)를 계산
     *
     * @param start 시작 시간
     * @param end   종료 시간
     * @return 순 방문자 수
     */
    // vl.visitor.id 와 vl.visitDate 로 필드명을 수정했습니다.
    @Query("SELECT COUNT(DISTINCT vl.visitor.userId) FROM VisitorLog vl WHERE vl.visitDate BETWEEN :start AND :end")
    long countDistinctVisitorByVisitDateBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // 특정 기간 동안의 순 방문자 수를 계산합니다.
    @Query("SELECT COUNT(DISTINCT vl.visitor.userId) FROM VisitorLog vl WHERE vl.visitDate BETWEEN :start AND :end")
    long countDistinctUserByVisitDateBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // ✨ 전체 기간에 대한 순 방문자 수를 계산합니다.
    @Query("SELECT COUNT(DISTINCT vl.visitor.userId) FROM VisitorLog vl")
    long countDistinctVisitor();
}