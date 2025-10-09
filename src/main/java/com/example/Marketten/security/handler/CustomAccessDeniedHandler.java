package com.example.Marketten.security.handler;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

/**
 * 인가(권한) 실패 핸들러입니다.
 * 인증은 되었으나(로그인 O), 해당 리소스에 접근할 권한이 없는 사용자가 접근할 때 호출됩니다.
 * 403 Forbidden JSON 응답을 반환하도록 재정의합니다.
 */
@Component
@Slf4j
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        log.warn("AccessDeniedHandler: User {} attempted to access restricted resource {}.", request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : "Anonymous", request.getRequestURI());

        // 403 Forbidden 상태 설정
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        // 응답 타입을 JSON으로 설정
        response.setContentType("application/json;charset=UTF-8");

        PrintWriter writer = response.getWriter();

        // 에러 응답 형식
        String jsonResponse = new Gson().toJson(
                Map.of("error", "ACCESS_DENIED", "message", "You do not have permission to access this resource.")
        );

        writer.println(jsonResponse);
    }
}