package com.example.Marketten.security.handler;

import com.example.Marketten.domain.Role;
import com.example.Marketten.oauth2.CustomOAuth2User;
import com.example.Marketten.util.JWTUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JWTUtil jwtUtil;  // JwtService 대신 JwtUtil

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        log.info("OAuth2 Login 성공!");

        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getEmail();

        try {
            // 신규 사용자(가입 필요) 판단: 필요 시 SubscribeType.FREE 또는 특정 조건 사용
            boolean isNewUser = oAuth2User.getRole() == null; // 또는 다른 판단 로직

            String accessToken = jwtUtil.generateAccessToken(email);

            if (isNewUser) {
                // 신규 가입 페이지로 redirect
                response.addHeader(jwtUtil.getAccessHeader(), "Bearer " + accessToken);
                response.sendRedirect("http://localhost:5173/auth/redirect?accessToken=" + accessToken);
            } else {
                // 기존 사용자: access + refresh 토큰 발급
                String refreshToken = jwtUtil.generateRefreshToken(email);
                response.addHeader(jwtUtil.getAccessHeader(), "Bearer " + accessToken);
                response.addHeader(jwtUtil.getRefreshHeader(), "Bearer " + refreshToken);

                // refreshToken 갱신
                jwtUtil.updateRefreshToken(email, refreshToken);

                // React 홈 페이지로 redirect
                response.sendRedirect("http://localhost:5173/auth/redirect?accessToken=" + accessToken);
            }

        } catch (Exception e) {
            log.error("OAuth2LoginSuccessHandler Error: ", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "OAuth2 로그인 처리 실패");
        }
    }}
