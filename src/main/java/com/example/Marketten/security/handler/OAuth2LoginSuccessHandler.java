// OAuth2 로그인 성공 시 처리 핸들러
package com.example.Marketten.security.handler;

import com.example.Marketten.domain.Status;
import com.example.Marketten.domain.User;
import com.example.Marketten.domain.VisitorLog;
import com.example.Marketten.oauth2.CustomOAuth2User;
import com.example.Marketten.repository.VisitorLogRepository;
import com.example.Marketten.service.LoginService;
import com.example.Marketten.util.JWTUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.function.Supplier;

@Slf4j
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JWTUtil jwtUtil; // JWT 토큰 생성 유틸리티
    private final Supplier<LoginService> loginServiceSupplier; // LoginService를 지연 초기화 방식으로 주입
    private final VisitorLogRepository visitorLogRepository; // 방문자 로그 저장소

    public OAuth2LoginSuccessHandler(JWTUtil jwtUtil, Supplier<LoginService> loginServiceSupplier, VisitorLogRepository visitorLogRepository) {
        this.jwtUtil = jwtUtil;
        this.loginServiceSupplier = loginServiceSupplier;
        this.visitorLogRepository = visitorLogRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        log.info("OAuth2 Login 성공!"); // 로그인 성공 로그 출력
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal(); // 인증된 사용자 정보 가져오기
        String email = oAuth2User.getEmail(); // 사용자 이메일 추출
        LoginService loginService = loginServiceSupplier.get(); // LoginService 인스턴스 획득

        try {
            User user = loginService.getUserByEmail(email); // 이메일로 사용자 정보 조회

            if (user.getStatus() != Status.ACTIVE) { // 사용자 계정 상태 확인
                response.sendRedirect("http://localhost:5173/login?error=account_inactive"); // 비활성 계정일 경우 로그인 페이지로 리디렉션
                return;
            }

            loginService.updateLastLogin(email); // 마지막 로그인 시간 업데이트

            VisitorLog log = VisitorLog.builder()
                    .visitor(user)
                    .visitDate(LocalDateTime.now())
                    .build(); // 방문자 로그 생성
            visitorLogRepository.save(log); // 방문자 로그 저장

            String accessToken = jwtUtil.generateAccessToken(email); // Access Token 생성

            // Refresh Token은 별도 API로 전달 (보안 및 길이 문제로 쿠키 사용 안함)

            Cookie accessTokenCookie = new Cookie("accessToken", accessToken); // Access Token을 쿠키로 설정
            accessTokenCookie.setPath("/"); // 모든 경로에서 쿠키 접근 가능
            accessTokenCookie.setMaxAge(60 * 60 * 7); // 쿠키 유효 시간 설정 (7시간)
            // accessTokenCookie.setHttpOnly(true); // JS 접근 가능하도록 HttpOnly 설정 생략

            response.addCookie(accessTokenCookie); // 쿠키를 응답에 추가

            response.sendRedirect("http://localhost:5173/auth/redirect?accessToken=" + accessToken); // 토큰을 URL 파라미터로 전달하여 리디렉션

        } catch (Exception e) {
            log.error("OAuth2LoginSuccessHandler Error: ", e); // 예외 발생 시 로그 출력
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "OAuth2 로그인 처리 실패"); // 서버 오류 응답 전송
        }
    }
}
