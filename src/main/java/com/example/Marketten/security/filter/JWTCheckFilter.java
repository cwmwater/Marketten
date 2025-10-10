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

    /**
     * 필터링을 건너뛸지 여부를 결정합니다.
     *
     * @return true: 필터링 건너뜀 (doFilterInternal 실행 안 함), false: 필터링 진행 (doFilterInternal 실행)
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        // Preflight 요청 (CORS 관련)은 필터 체크하지 않음 -> 제외
        if (request.getMethod().equals("OPTIONS")) {
            log.info("******* JWTCheckFilter - shouldNotFilter : OPTIONS request -> True (Method: OPTIONS)");
            return true;
        }

        String requestURI = request.getRequestURI();
        log.info("******* JWTCheckFilter - shouldNotFilter : Request URI -> {}", requestURI);

        // 회원 관련 요청 경로 -> /api/auth/** 로 통일 (로그인/가입 경로)
        if (requestURI.startsWith("/api/auth/")) {
            log.info("******* JWTCheckFilter - shouldNotFilter : Path /api/auth/** matched -> True (Permitted Path)");
            return true;
        }

        // ✨ 소셜 로그인 관련 경로 추가: 토큰 검증 면제
        if (requestURI.startsWith("/oauth2/authorization/") ||
                requestURI.startsWith("/login/oauth2/code/")) {
            log.info("******* JWTCheckFilter - shouldNotFilter : Path OAuth2 matched -> True (Permitted Path)");
            return true;
        }


        // 이미지 리소스 요청 경로 -> 제외 (필요 시 추가)
        if (requestURI.startsWith("/api/products/image/")) {
            log.info("******* JWTCheckFilter - shouldNotFilter : Path /api/products/image/ matched -> True");
            return true;
        }
        if (requestURI.startsWith("/api/mkt/v1/temp/")) return true;
        if (requestURI.startsWith("/api/mkt/v1/post/")) return true;

        log.info("******* JWTCheckFilter - shouldNotFilter : No path matched -> False (Filtering required)");
        return false; // 나머지는 필터링해~
    }//shouldNotFilter

    /**
     * JWT 토큰 검증 및 인증 객체 등록 로직
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // shouldNotFilter()가 false를 반환했을 때만 이 코드가 실행됩니다.
        log.info("*********** JWTCheckFilter - doFilterInternal (JWT 검증 시작)");

        String authHeader = request.getHeader("Authorization");

        // Bearer 토큰 형식 체크
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("********** JWTCheckFilter - Authorization header missing or malformed. Header Value: {}", authHeader); // 💥 어떤 값이 들어왔는지 로그 남기기
            handleAuthError(response);
            return;
        }

        String accessToken = authHeader.substring(7); // access token 추출

        try {
            // 1. AccessToken 검증 및 Claims 추출
            Map<String, Object> claims = jwtUtil.validateToken(accessToken);
            log.info("******** JWTCheckFilter - doFilterInternal claims : {}", claims);

            String email = (String) claims.get("email");

            // 2. 이메일로 DB에서 사용자 정보 조회
            Optional<User> result = userRepository.findByEmail(email);

            // 사용자 정보가 DB에 없으면 예외 처리
            User user = result.orElseThrow(() -> new RuntimeException("User not found by JWT email: " + email));

            // 3. 시큐리티용 사용자 정보 객체로 변환
            CustomUserDetails userDetails = new CustomUserDetails(user);

            // 4. 시큐리티용 인증 토큰 생성
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            // 5. 시큐리티 컨텍스트에 추가
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);

            filterChain.doFilter(request, response); // 다음 필터 체인으로 진행

        } catch (CustomJWTException e) {
            log.warn("JWT 검증 실패 - error message : {}", e.getMessage());
            handleAuthError(response);
        } catch (RuntimeException e) {
            log.error("사용자 정보 로드 실패: {}", e.getMessage());
            handleAuthError(response);
        }
    }//doFilterInternal

    /**
     * access token 검사 예외 처리 메서드: 에러 메시지 응답하기
     */
    private void handleAuthError(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter writer = response.getWriter();
        // 토큰 에러 메시지 반환
        writer.println(new Gson().toJson(Map.of("error", "ERROR_ACCESS_TOKEN", "message", "Invalid or expired token")));
    }

}//class