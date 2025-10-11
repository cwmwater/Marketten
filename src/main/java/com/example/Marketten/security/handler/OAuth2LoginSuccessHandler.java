package com.example.Marketten.security.handler;

import com.example.Marketten.domain.User;
import com.example.Marketten.dto.auth.TokenInfo;
import com.example.Marketten.repository.UserRepository;
import com.example.Marketten.service.LoginService;
import com.example.Marketten.util.JWTUtil;
import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JWTUtil jwtUtil;
    private final LoginService loginService;  // LoginServiceImpl 대신 인터페이스 주입
    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        log.info("OAuth2 Login 성공!");

        String email = null;

        try {
            // CustomOAuth2User에서 이메일 추출
            Object principal = authentication.getPrincipal();

            if (principal instanceof org.springframework.security.oauth2.core.user.OAuth2User) {
                org.springframework.security.oauth2.core.user.OAuth2User oAuth2User =
                        (org.springframework.security.oauth2.core.user.OAuth2User) principal;

                // CustomOAuth2User 타입인지 확인
                if (principal instanceof com.example.Marketten.oauth2.CustomOAuth2User) {
                    email = ((com.example.Marketten.oauth2.CustomOAuth2User) principal).getEmail();
                    log.info("CustomOAuth2User에서 이메일 추출: {}", email);
                } else {
                    // CustomOAuth2User가 아닌 경우 attributes에서 직접 추출
                    email = (String) oAuth2User.getAttribute("email");
                    log.info("OAuth2User attributes에서 이메일 추출: {}", email);
                }
            }

            if (email == null || email.isEmpty()) {
                throw new RuntimeException("이메일 정보를 가져올 수 없습니다.");
            }

            // DB에서 User 정보 조회
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

            log.info("DB에서 사용자 정보 조회 완료. Email: {}", email);

            // LoginService를 통해 토큰 생성 및 Redis에 저장
            TokenInfo tokenInfo = loginService.generateTokenForUser(user);

            log.info("OAuth2 로그인 - 토큰 생성 완료. Email: {}, AccessToken: {}, RefreshToken: {}",
                    email,
                    tokenInfo.getAccessToken().substring(0, 20) + "...",
                    tokenInfo.getRefreshToken().substring(0, 20) + "...");

            // 클라이언트에 JSON 응답 (일반 로그인과 동일한 형식)
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(new Gson().toJson(tokenInfo));

        } catch (Exception e) {
            log.error("OAuth2LoginSuccessHandler Error: ", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(new Gson().toJson(
                    Map.of(
                            "error", "OAUTH2_LOGIN_FAILED",
                            "message", e.getMessage() != null ? e.getMessage() : "OAuth2 로그인 처리 실패"
                    )
            ));
        }
    }
}