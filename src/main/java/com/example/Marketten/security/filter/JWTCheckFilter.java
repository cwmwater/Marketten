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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class JWTCheckFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        // 1️⃣ OPTIONS (CORS) 요청 제외
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            log.info("[JWTCheckFilter] OPTIONS 요청 → 필터 제외");
            return true;
        }

        String path = request.getServletPath();
        log.info("[JWTCheckFilter] shouldNotFilter - path = {}", path);

        // 2️⃣ 인증 필요 없는 경로 배열
        String[] excludedPaths = {
                "/api/auth",
                "/api/temp",
                "/api/posts",
                "/api/products/image",
                "/oauth2/authorization",
                "/login/oauth2/code",
                "/",
                "/favicon.ico"
        };

        // 3️⃣ 슬래시 유무 상관없이 체크
        for (String exclude : excludedPaths) {
            if (path.equals(exclude) || path.startsWith(exclude + "/")) {
                log.info("[JWTCheckFilter] Excluded path matched: {}", exclude);
                return true;
            }
        }

        log.info("[JWTCheckFilter] → JWT 필터 적용 대상");
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        log.info("[JWTCheckFilter] doFilterInternal - requestURI: {}", request.getRequestURI());

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {

            log.warn("********** JWTCheckFilter - Authorization header missing or malformed. Header Value: {}", authHeader);
            handleAuthError(response);
            return; // ⬅️ 오류 발생 시 여기서 요청 처리를 중단합니다.
        }

        String accessToken = authHeader.substring(7);

        try {
            Map<String, Object> claims = jwtUtil.validateToken(accessToken);
            log.info("[JWTCheckFilter] JWT claims: {}", claims);

            String email = (String) claims.get("email");
            Optional<User> result = userRepository.findByEmail(email);

            // 🚨 사용자가 존재하지 않으면 401 응답을 보내고 즉시 중단합니다.
            if (result.isEmpty()) {
                log.warn("User not found in DB with email: {}", email);
                handleAuthError(response);
                return; // ⬅️ 즉시 중단
            }

            User user = result.get();

            CustomUserDetails userDetails = new CustomUserDetails(user);
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(authenticationToken);

            filterChain.doFilter(request, response); // 다음 필터 체인으로 진행 (이후 필터가 간섭하지 않도록 처리)

        } catch (CustomJWTException e) {
            // 토큰 만료/변조 시 401 응답 후 중단
            log.warn("JWT validation failed: {}", e.getMessage());
            handleAuthError(response);
            return; // ⬅️ 오류 발생 시 여기서 중단합니다.
        } catch (Exception e) {
            // 기타 예상치 못한 오류 시 401 응답 후 중단
            log.error("Unexpected error during JWT processing: {}", e.getMessage());
            handleAuthError(response);
            return; // ⬅️ 오류 발생 시 여기서 중단합니다.
        }
    }

    private void handleAuthError(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter writer = response.getWriter();
        writer.println(new Gson().toJson(Map.of(
                "error", "ERROR_ACCESS_TOKEN",
                "message", "Invalid or expired token"
        )));
    }
}