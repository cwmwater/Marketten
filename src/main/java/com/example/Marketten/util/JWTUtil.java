package com.example.Marketten.util;

import com.example.Marketten.exception.CustomJWTException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.time.ZonedDateTime;

@Slf4j
@Component
public class JWTUtil {

    @Value("${jwt.secret}")
    private String key;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        log.info("JWTUtil KEY init : {}", key);
        byte[] decoded = Base64.getDecoder().decode(key);
        // HS256을 명시적으로 사용 (토큰 생성과 검증 일치)
        this.secretKey = Keys.hmacShaKeyFor(decoded);
    }

    // 토큰 생성 메서드 : accessToken, refreshToken
    public String generateToken(Map<String, Object> claims, int expireMinutes) {
        String jwtStr = Jwts.builder()
                .setHeader(Map.of("typ", "JWT")) // header
                // payload
                .setClaims(claims) // 사용자 정보
                .setIssuedAt(Date.from(ZonedDateTime.now().toInstant())) // 발생시간
                .setExpiration(Date.from(ZonedDateTime.now().plusMinutes(expireMinutes).toInstant())) // 유효시간
                .signWith(secretKey, SignatureAlgorithm.HS256) // ← HS256 명시
                .compact();
        log.info("jwtStr : {}", jwtStr);
        return jwtStr;
    }

    // AccessToken 생성
    public String generateAccessToken(String email) {
        return generateToken(Map.of("email", email), 60); // 60분
    }

    // RefreshToken 생성
    public String generateRefreshToken(String email) {
        return generateToken(Map.of("email", email), 60 * 24 * 7); // 7일
    }

    public String getAccessHeader() {
        return "Authorization";
    }

    public String getRefreshHeader() {
        return "RefreshToken";
    }

    // 토큰 검증 메서드 : 검증 후 Claims 리턴
    public Map<String, Object> validateToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token) // 검증 : 예외발생가능
                    .getBody();  // claims 꺼내기
        }catch (ExpiredJwtException e) { // 만료된 토큰
            throw new CustomJWTException("Expired");
        }catch (MalformedJwtException e) { // 잘못된 형식의 토큰
            throw new CustomJWTException("Malformed");
        }catch (InvalidClaimException e) { // 유효하지 않은 claims
            throw new CustomJWTException("Invalid");
        }catch (JwtException e) { // 그 외 JWT 예외
            log.info("jwtException : {}", e.getMessage());
            throw new CustomJWTException("JWTError");
        }catch (Exception e) {  // 그 나머지 예외
            log.info("Exception : {}", e.getMessage());
            throw new CustomJWTException("Error");
        }
    }

    /**
     * 토큰의 유효성만 확인하고 결과를 boolean으로 반환합니다.
     * validateToken() 메서드를 호출하여 예외가 발생하면 false를 반환합니다.
     *
     * @param token 검증할 JWT 토큰
     * @return 유효하면 true, 아니면 false
     */
    public boolean isTokenValid(String token) {
        try {
            validateToken(token);
            return true;
        } catch (CustomJWTException e) {
            // 토큰이 만료되거나 형식이 잘못된 경우
            return false;
        } catch (Exception e) {
            // 그 외 예외
            return false;
        }
    }

    public String parseEmailFromToken(String token) {
        Map<String, Object> claims = validateToken(token);
        return (String) claims.get("email");
    }
}