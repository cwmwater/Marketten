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
                "/api/post",
                "/api/products/image",
                "/oauth2/authorization",
                "/login/oauth2/code",
                "/", "/favicon.ico"
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
            log.warn("[JWTCheckFilter] Authorization header missing or malformed. Value: {}", authHeader);
            handleAuthError(response);
            return;
        }

        String accessToken = authHeader.substring(7);

        try {
            Map<String, Object> claims = jwtUtil.validateToken(accessToken);
            log.info("[JWTCheckFilter] JWT claims: {}", claims);

            String email = (String) claims.get("email");
            Optional<User> result = userRepository.findByEmail(email);
            User user = result.orElseThrow(() -> new RuntimeException("User not found by JWT email: " + email));

            CustomUserDetails userDetails = new CustomUserDetails(user);
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(authenticationToken);

            filterChain.doFilter(request, response);

        } catch (CustomJWTException e) {
            log.warn("[JWTCheckFilter] JWT 검증 실패: {}", e.getMessage());
            handleAuthError(response);
        } catch (RuntimeException e) {
            log.error("[JWTCheckFilter] 사용자 정보 로드 실패: {}", e.getMessage());
            handleAuthError(response);
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
