package com.example.Marketten.repository;

import com.example.Marketten.domain.RefreshToken;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

// CrudRepository를 상속받아 Redis 데이터 관리를 위한 기본적인 CRUD 메서드를 제공받습니다.
// <엔티티 클래스, 엔티티 ID 타입>
public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {

    // 리프레시 토큰 값(String)으로 RefreshToken 엔티티를 찾아 반환하는 메서드
    // 로그아웃 및 토큰 재발급 시 Redis에 해당 토큰이 있는지 확인하는 데 사용됩니다.
    Optional<RefreshToken> findByRefreshToken(String refreshToken);
}
