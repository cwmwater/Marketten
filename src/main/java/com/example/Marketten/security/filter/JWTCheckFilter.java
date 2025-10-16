package com.example.Marketten.security.filter;

import com.example.Marketten.domain.User;
import com.example.Marketten.exception.CustomJWTException;
import com.example.Marketten.repository.UserRepository;
import com.example.Marketten.security.CustomUserDetails;
import com.example.Marketten.util.JWTUtil;
import com.google.gson.Gson;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class JWTCheckFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String requestURI = request.getRequestURI();

        // ✨ 이미지 경로는 이 필터가 아예 검사하지 않도록 명시적으로 제외합니다. (성능 향상)
        if (requestURI.startsWith("/images/")) {
            return true;
        }

        // 기존의 다른 필터 제외 경로들
        if (request.getMethod().equals("OPTIONS") ||
                requestURI.startsWith("/api/auth/") ||
                requestURI.startsWith("/oauth2/authorization/") ||
                requestURI.startsWith("/login/oauth2/code/")) {
            return true;
        }

        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        // =========================================================================
        // ▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼ 수정된 부분 ▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼
        // =========================================================================
        // ✨ [가장 중요한 수정]
        // Authorization 헤더가 없거나, 'Bearer '로 시작하지 않으면
        // 에러를 보내지 않고, 그냥 다음 필터로 요청을 넘깁니다. (공개 경로 접근 허용)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return; // ✨ 여기서 바로 함수를 종료합니다.
        }
        // =========================================================================
        // ▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲
        // =========================================================================

        // --- 이하 토큰 검증 로직은 기존과 동일 ---
        try {
            String accessToken = authHeader.substring(7);
            Map<String, Object> claims = jwtUtil.validateToken(accessToken);
            String email = (String) claims.get("email");

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new CustomJWTException("User not found"));

            CustomUserDetails userDetails = new CustomUserDetails(user);

            Authentication authenticationToken = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities()
            );

            SecurityContextHolder.getContext().setAuthentication(authenticationToken);

            filterChain.doFilter(request, response);

        } catch (Exception e) {
            // 토큰이 유효하지 않을 때 (만료, 변조 등) 401 에러 응답
            log.error("JWT Check Error: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            PrintWriter writer = response.getWriter();
            writer.println(new Gson().toJson(Map.of("error", "INVALID_TOKEN")));
            return;
        }
    }
}