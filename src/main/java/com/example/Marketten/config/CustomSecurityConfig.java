package com.example.Marketten.config;

import com.example.Marketten.repository.UserRepository;
import com.example.Marketten.security.filter.JWTCheckFilter;
import com.example.Marketten.security.handler.CustomAccessDeniedHandler;
import com.example.Marketten.security.handler.CustomAuthenticationEntryPoint;
import com.example.Marketten.security.handler.OAuth2LoginFailureHandler;
import com.example.Marketten.security.handler.OAuth2LoginSuccessHandler;
import com.example.Marketten.service.CustomOAuth2UserService;
import com.example.Marketten.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

import java.util.Arrays;

@Configuration
@Slf4j
@EnableMethodSecurity
@RequiredArgsConstructor
public class CustomSecurityConfig {

    private final JWTUtil jwtUtil;
    private final UserRepository userRepository;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final OAuth2LoginFailureHandler oAuth2LoginFailureHandler;

    // --- 1. CORS Filter Bean 등록 (Security Filter Chain 외부로 분리) ---
    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilterRegistration(CorsConfigurationSource corsConfigurationSource) {
        FilterRegistrationBean<CorsFilter> registration = new FilterRegistrationBean<>(new CorsFilter(corsConfigurationSource));
        registration.setOrder(0);
        return registration;
    }

    // --- 2. 단일 필터 체인 (JWT/OAuth2) ---
    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            CustomOAuth2UserService customOAuth2UserService
    ) throws Exception {

        // Session STATELESS 설정 (JWT 사용)
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // CSRF 비활성화 (CORS는 FilterRegistrationBean에서 처리)
        http.csrf(csrf -> csrf.disable());

        // 🚨 인증/인가 실패 핸들러 설정: 401/403 JSON 응답 강제
        http.exceptionHandling(exceptionHandling -> exceptionHandling
                .authenticationEntryPoint(new CustomAuthenticationEntryPoint())
                .accessDeniedHandler(new CustomAccessDeniedHandler())
        );

        // 1. 요청 경로 권한 설정 (가장 중요: permitAll()을 먼저 선언)
        http.authorizeHttpRequests(auth -> auth
                // 로그인, 회원가입, 토큰 재발급 등 인증 관련 경로는 모두 허용
                .requestMatchers("/api/auth/**", "/api/temp/**", "/api/post/**", "/api/products/image/**").permitAll()
                // 그 외 모든 요청은 인증 필요
                .anyRequest().authenticated());

        // 2. JWT 체크 필터 적용
        http.addFilterBefore(
                new JWTCheckFilter(jwtUtil, userRepository),
                SecurityContextHolderFilter.class
        );

        // 3. 구글 OAuth2 로그인 활성화
        http.oauth2Login(oauth2 -> oauth2
                .successHandler(oAuth2LoginSuccessHandler)
                .failureHandler(oAuth2LoginFailureHandler)
                .userInfoEndpoint(userInfo ->
                        userInfo.userService(customOAuth2UserService)
                )
        );

        return http.build();
    }

    // --- 기타 Bean 정의 ---

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("HEAD", "GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}