package com.example.Marketten.security.filter;

import com.example.Marketten.exception.CustomJWTException;
import com.example.Marketten.repository.UserRepository;
import com.example.Marketten.util.JWTUtil;
import com.google.gson.Gson;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class JWTCheckFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;
    private final UserRepository userRepository;

    // 필터링 제외 시키는 설정
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        // Preflight 요청은 필터 체크하지 않음 -> 제외
        if(request.getMethod().equals("OPTIONS")) {
            return true;
        }

        // 요청 경로를 검사해서 필터링 제외
        String requestURI = request.getRequestURI();
        log.info("******* JWTCheckFilter - shouldNotFilter : requestURI : {}", requestURI);

        // 회원 관련 요청 경로 -> 제외
        if(requestURI.startsWith("/api/members/")) {
            return true;
        }
        // 이미지 리소스 요청 경로 -> 제외
        if(requestURI.startsWith("/api/products/image/")) {
            return true;
        }
        if(requestURI.startsWith("/api/mkt/v1/temp/")) return true;
        if(requestURI.startsWith("/api/mkt/v1/post/")) return true;
        return false; // 나머지는 필터링해~
    }//shouldNotFilter

    // 필터링 작업 로직 : 1. 유효한 access token 검증 -> claims 리턴  2. 사용자 정보 다시 조회 -> 시큐리티 인증 객체 생성/적용
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.info("*********** JWTCheckFilter - doFilterInternal");

        String authHeader = request.getHeader("Authorization");
        // Bearer xxxxxx....accessToken문자열 : 문자열 index 7부터 끝까지 access token 값임
        if(authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("********** JWTCheckFilter - Authorization header missing or malformed");
            // 예외 메세지 응답
            handleAuthError(response);
            return; // 필터 메서드 강제 종료!
        }


        String accessToken = authHeader.substring(7); // access token 추출

        try {
           /* Map<String, Object> claims = jwtUtil.validateToken(accessToken); // accessToken 검증 -> 예외 발생
            log.info("******** JWTCheckFilter - doFilterInternal claims : {}", claims);

            String email = (String) claims.get("email");
            Member member = memberRepository.getMemberByEmailWithRoles(email)
                    .orElseThrow(() -> new RuntimeException("Member not found by JWT email"));

            // 시큐리티용 사용자 정보 객체로 변환
            CustomUserDetails userDetails = new CustomUserDetails(member);
            // 시큐리티용 토큰 생성
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            // 시큐리티 컨텍스트에 추가
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);

            filterChain.doFilter(request, response); // 다음 필터 체인으로 진행해~*/
        }catch (CustomJWTException e) {
            log.warn("JWT 검증 실패 - error message : {}", e.getMessage());
            handleAuthError(response);
        }
    }//doFilterInternal

    // access token 검사 예외 처리 메서드 : 에러 메세지 응답하기
    private void handleAuthError(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter writer = response.getWriter();
        writer.println(new Gson().toJson(Map.of("error", "ERROR_ACCESS_TOKEN")));
    }

}//class
