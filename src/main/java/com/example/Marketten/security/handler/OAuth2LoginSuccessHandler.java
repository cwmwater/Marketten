package com.example.Marketten.security.handler;

import com.example.Marketten.domain.User;
import com.example.Marketten.domain.VisitorLog;
import com.example.Marketten.oauth2.CustomOAuth2User;
import com.example.Marketten.repository.VisitorLogRepository;
import com.example.Marketten.service.LoginService;
import com.example.Marketten.util.JWTUtil;
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

    private final JWTUtil jwtUtil;
    private final Supplier<LoginService> loginServiceSupplier;
    private final VisitorLogRepository visitorLogRepository;

    /**
     * 생성자: 모든 의존성을 수동으로 주입받습니다.
     */
    public OAuth2LoginSuccessHandler(JWTUtil jwtUtil, Supplier<LoginService> loginServiceSupplier, VisitorLogRepository visitorLogRepository) {
        this.jwtUtil = jwtUtil;
        this.loginServiceSupplier = loginServiceSupplier;
        this.visitorLogRepository = visitorLogRepository;
        log.info("OAuth2LoginSuccessHandler initialized with LoginService Supplier and VisitorLogRepository.");
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        log.info("OAuth2 Login 성공!");

        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getEmail();

        LoginService loginService = loginServiceSupplier.get();

        try {
            // 1. DB에서 User 객체 로드
            User user = loginService.getUserByEmail(email);

            VisitorLog log = VisitorLog.builder()
                    .visitor(user)
                    .visitDate(LocalDateTime.now())
                    .build();
            visitorLogRepository.save(log);

            // 2. Access Token 발급
            String accessToken = jwtUtil.generateAccessToken(email);

            // 3. Refresh Token 조회
            String refreshToken = loginService.getRefreshTokenByEmail(email);

            // 토큰 헤더에 추가
            response.addHeader(jwtUtil.getAccessHeader(), "Bearer " + accessToken);
            response.addHeader(jwtUtil.getRefreshHeader(), "Bearer " + refreshToken);

            // 임시 리다이렉트
            response.sendRedirect("http://localhost:8080/"); // 프론트엔드 주소로 변경 필요

        } catch (Exception e) {
            log.error("OAuth2LoginSuccessHandler Error: ", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "OAuth2 로그인 처리 실패");
        }
    }
}