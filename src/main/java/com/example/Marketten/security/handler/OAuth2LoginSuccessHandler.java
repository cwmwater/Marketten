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
import lombok.Value;
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



    public OAuth2LoginSuccessHandler(JWTUtil jwtUtil, Supplier<LoginService> loginServiceSupplier, VisitorLogRepository visitorLogRepository) {
        this.jwtUtil = jwtUtil;
        this.loginServiceSupplier = loginServiceSupplier;
        this.visitorLogRepository = visitorLogRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        log.info("OAuth2 Login 성공!");
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getEmail();
        LoginService loginService = loginServiceSupplier.get();

        try {
            User user = loginService.getUserByEmail(email);

            if (user.getStatus() != Status.ACTIVE) {
                // ... (상태 확인 및 에러 리다이렉트 로직은 동일)
                response.sendRedirect("http://marketten.shop/login?error=account_inactive");
                return;
            }

            loginService.updateLastLogin(email);

            VisitorLog log = VisitorLog.builder()
                    .visitor(user)
                    .visitDate(LocalDateTime.now())
                    .build();
            visitorLogRepository.save(log);

            String accessToken = jwtUtil.generateAccessToken(email);
            // Refresh Token은 쿠키로 전달하기에는 너무 길고 중요하므로,
            // 필요하다면 별도의 API를 통해 가져오게 하는 것이 더 안전

            Cookie accessTokenCookie = new Cookie("accessToken", accessToken);
            accessTokenCookie.setPath("/"); // "/" 경로 이하 모든 페이지에서 쿠키 접근 가능
            accessTokenCookie.setMaxAge(60 * 60 * 7); // 쿠키 유효 시간 (초 단위, 예: 60초)
            // accessTokenCookie.setHttpOnly(true); // HttpOnly를 true로 하면 JS에서 접근 불가하므로, 여기서는 false(기본값)로 둡니다.

            response.addCookie(accessTokenCookie);


// ✅ accessToken을 URL 파라미터로 전달
            response.sendRedirect("http://marketten.shop/auth/redirect?accessToken=" + accessToken);



        } catch (Exception e) {
            log.error("OAuth2LoginSuccessHandler Error: ", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "OAuth2 로그인 처리 실패");
        }
    }
}