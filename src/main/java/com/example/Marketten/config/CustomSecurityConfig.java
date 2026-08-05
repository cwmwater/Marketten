package com.example.Marketten.config;

import com.example.Marketten.repository.UserRepository;
import com.example.Marketten.repository.VisitorLogRepository;
import com.example.Marketten.security.filter.JWTCheckFilter;
import com.example.Marketten.security.handler.CustomAccessDeniedHandler;
import com.example.Marketten.security.handler.CustomAuthenticationEntryPoint;
import com.example.Marketten.security.handler.OAuth2LoginFailureHandler;
import com.example.Marketten.security.handler.OAuth2LoginSuccessHandler;
import com.example.Marketten.service.CustomOAuth2UserService;
import com.example.Marketten.service.LoginService;
import com.example.Marketten.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.function.Supplier;

@Configuration
@Slf4j
@EnableMethodSecurity
@RequiredArgsConstructor
public class CustomSecurityConfig {

    static {
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
    }

    private final JWTUtil jwtUtil;
    private final UserRepository userRepository;
    private final OAuth2LoginFailureHandler oAuth2LoginFailureHandler;

    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilterRegistration(CorsConfigurationSource corsConfigurationSource) {
        FilterRegistrationBean<CorsFilter> registration = new FilterRegistrationBean<>(new CorsFilter(corsConfigurationSource));
        registration.setOrder(0);
        return registration;
    }

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            CustomOAuth2UserService customOAuth2UserService,
            ApplicationContext applicationContext,
            VisitorLogRepository visitorLogRepository
    ) throws Exception {

        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.csrf(AbstractHttpConfigurer::disable);
        http.anonymous(AbstractHttpConfigurer::disable);
        http.exceptionHandling(exceptionHandling -> exceptionHandling
                .authenticationEntryPoint(new CustomAuthenticationEntryPoint())
                .accessDeniedHandler(new CustomAccessDeniedHandler())
        );

        Supplier<LoginService> loginServiceSupplier = () -> applicationContext.getBean(LoginService.class);

        OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler = new OAuth2LoginSuccessHandler(
                jwtUtil,
                loginServiceSupplier,
                visitorLogRepository
        );

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(
                        // Spring Boot의 기본 에러 페이지 경로(/error)를 인증 없이 허용
                        // 이 규칙이 없으면, 다른 에러 발생 시 이중으로 보안 에러 발생
                        AntPathRequestMatcher.antMatcher("/api/common/**"),
                        AntPathRequestMatcher.antMatcher("/favicon.ico"),
                        AntPathRequestMatcher.antMatcher("/error"),
                        AntPathRequestMatcher.antMatcher("/images/**"),
                        AntPathRequestMatcher.antMatcher("/api/auth/**"),
                        AntPathRequestMatcher.antMatcher("/oauth2/authorization/**"),
                        AntPathRequestMatcher.antMatcher("/login/oauth2/code/**"),
                        AntPathRequestMatcher.antMatcher("/api/temp/**"),
                        AntPathRequestMatcher.antMatcher("/api/posts/**"),
                        AntPathRequestMatcher.antMatcher("/api/products/image/**")
                ).permitAll()
                .anyRequest().authenticated());

        http.addFilterBefore(
                new JWTCheckFilter(jwtUtil, userRepository),
                UsernamePasswordAuthenticationFilter.class
        );

        http.oauth2Login(oauth2 -> oauth2
                .successHandler(oAuth2LoginSuccessHandler)
                .failureHandler(oAuth2LoginFailureHandler)
                .userInfoEndpoint(userInfo ->
                        userInfo.userService(customOAuth2UserService)
                )
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
        configuration.setAllowedMethods(Arrays.asList("HEAD", "GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type", "email"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }


}