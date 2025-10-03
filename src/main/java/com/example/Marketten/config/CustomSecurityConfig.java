package com.example.Marketten.config;

import com.example.Marketten.repository.UserRepository;
import com.example.Marketten.security.filter.JWTCheckFilter;
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
import org.springframework.security.web.context.SecurityContextHolderFilter; // ✨ SecurityContextHolderFilter 임포트 추가
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

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

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            CustomOAuth2UserService customOAuth2UserService
    ) throws Exception {

        // Session STATELESS 설정 (JWT 사용)
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // CORS 설정 및 CSRF 비활성화
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));
        http.csrf(csrf -> csrf.disable());


        // 1. 요청 경로 권한 설정 활성화 (가장 먼저 처리)
        http.authorizeHttpRequests(auth -> auth
                // 로그인, 회원가입 등 인증 경로는 모두 허용
                .requestMatchers("/api/auth/**").permitAll()
                // 그 외 모든 요청은 인증 필요
                .anyRequest().authenticated());

        // 2. 구글 OAuth2 로그인 활성화
        http.oauth2Login(oauth2 -> oauth2
                .successHandler(oAuth2LoginSuccessHandler)
                .failureHandler(oAuth2LoginFailureHandler)
                .userInfoEndpoint(userInfo ->
                        userInfo.userService(customOAuth2UserService)
                )
        );

        // 3. JWT 체크 필터 적용 위치 변경
        // 가장 안전하게 permitAll()이 적용되도록 SecurityContextHolderFilter 앞에 위치시킵니다.
        http.addFilterBefore(
                new JWTCheckFilter(jwtUtil, userRepository),
                SecurityContextHolderFilter.class // ✨ 필터 위치 변경
        );

        return http.build();
    }

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
