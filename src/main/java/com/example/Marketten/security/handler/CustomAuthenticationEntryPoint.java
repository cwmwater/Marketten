package com.example.Marketten.security.handler;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

/**
 * 인증되지 않은 사용자(토큰 없음/만료/잘못됨)가 보호된 리소스에 접근할 때 호출됩니다.
 * 기본 동작인 /login 리다이렉트 대신 401 Unauthorized JSON 응답을 반환하도록 재정의합니다.
 */
@Component
@Slf4j
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        log.warn("AuthenticationEntryPoint: Unauthorized access attempt to protected resource. URI: {}", request.getRequestURI());

        // 401 Unauthorized 상태 설정
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        // 응답 타입을 JSON으로 설정
        response.setContentType("application/json;charset=UTF-8");

        PrintWriter writer = response.getWriter();

        // JWT 필터에서 사용하는 것과 유사한 에러 응답 형식으로 통일
        String jsonResponse = new Gson().toJson(
                Map.of("error", "AUTHENTICATION_REQUIRED", "message", "Authentication required to access this resource.")
        );

        writer.println(jsonResponse);
    }
}