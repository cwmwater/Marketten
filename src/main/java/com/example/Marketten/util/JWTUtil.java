package com.example.Marketten.util;

import com.example.Marketten.exception.CustomJWTException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

@Slf4j
@Component
public class JWTUtil {

    @Value("${jwt.secret}")
    private String key;
    //private static final String SECRET = "bXlzZWNyZXRrZXlteXNlY3JldGtleW15c2VjcmV0a2V5bXlzZWNyZXRrZXlteXNlY3JldGtleW15c2VjcmV0a2V5";

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        log.info("JWTUtil KEY init : {}", key);
        byte[] decoded = Base64.getDecoder().decode(key);
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
                .signWith(secretKey) // 비밀키로 서명
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

    // refreshToken DB 저장용 (UserRepository 주입 필요)
    public void updateRefreshToken(String email, String refreshToken) {
        // userRepository.updateRefreshToken(email, refreshToken);
        // 또는 DB 업데이트 코드 삽입
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
    public String parseEmailFromToken(String token) {
        Map<String, Object> claims = validateToken(token);
        return (String) claims.get("email");
    }









}
