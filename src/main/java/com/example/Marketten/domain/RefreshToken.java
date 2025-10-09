package com.example.Marketten.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

// Redis에 저장될 객체임을 명시하며, "refreshToken" 이라는 키 아래에 저장됨
@RedisHash("refreshToken")
@Getter
@Builder
@AllArgsConstructor
public class RefreshToken {

    // 사용자의 이메일을 ID로 사용 (Unique Key)
    @Id
    private String id; // email

    // 실제 리프레시 토큰 값
    private String refreshToken;

    // 토큰 발급 시 함께 발급된 Access Token (선택적: 비교/확인용)
    private String accessToken;

    // 만료 시간 (Time To Live). Redis에서 이 시간이 지나면 자동으로 삭제됨 (초 단위)
    @TimeToLive
    private Long expiration;

    /**
     * 리프레시 토큰의 만료 시간을 설정합니다.
     *
     * @param expiration 만료 시간 (초)
     * @return 갱신된 RefreshToken 객체
     */
    public RefreshToken updateExpiration(Long expiration) {
        this.expiration = expiration;
        return this;
    }
}
