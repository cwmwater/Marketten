package com.example.Marketten.config;

import com.example.Marketten.repository.UserRepository;
import com.example.Marketten.security.handler.*;
import com.example.Marketten.service.CustomOAuth2UserService;
import com.example.Marketten.util.JWTUtil;
/*import com.react.Marketten.repository.MemberRepository;
import com.react.Marketten.security.filter.JWTCheckFilter;
import com.react.Marketten.security.handler.APILoginFailureHandler;
import com.react.Marketten.security.handler.APILoginSuccessHandler;
import com.react.Marketten.security.handler.CustomAccessDeniedHandler;
import com.react.Marketten.util.JWTUtil;*/
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
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
    private final CustomOAuth2UserService customOAuth2UserService;
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        log.info("******************* Spring Security Config ******************");
        // cors 설정 등록
        http.cors(corsConf -> {
            corsConf.configurationSource(corsConfigurationSource());
        });
        // csrf 비활성화
        http.csrf(csrf -> csrf.disable());
        // 세션 stateless 상태로 설정
        http.sessionManagement(sessionMng -> sessionMng.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        //http.sessionManagement(sessionMng -> sessionMng.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED));
        // 요청 경로 권한 설정
        /*http.authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll());*/

        // 구글 OAuth2 로그인 활성화
        http.oauth2Login(oauth2 -> oauth2
                .successHandler(oAuth2LoginSuccessHandler)  // 로그인 성공 시 JWT 발급 + React redirect
                .failureHandler(oAuth2LoginFailureHandler)  // 로그인 실패 시 처리
                .userInfoEndpoint(userInfo ->
                        userInfo.userService(customOAuth2UserService) // OAuth2User 정보 가져오는 Custom Service
                )
        );
        // 로그인 설정
//        http.formLogin(login -> {
//            login.loginPage("/api/members/login"); // 로그인 처리 경로 설정
//            login.successHandler(new APILoginSuccessHandler(jwtUtil)); // 로그인 성공 핸들러 추가
//            login.failureHandler(new APILoginFailureHandler()); // 로그인 실패 핸들러 추가
//        });


        // JWT 체크 필터 적용
//        http.addFilterBefore(new JWTCheckFilter(jwtUtil, userRepository), UsernamePasswordAuthenticationFilter.class);

        // 접근제한 예외 처리 클래스 적용
//        http.exceptionHandling(exception -> {
//            exception.accessDeniedHandler(new CustomAccessDeniedHandler());
//        });


        return http.build();
    }//filterChain

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*", "http://localhost:5173"));
        configuration.setAllowedMethods(Arrays.asList("HEAD", "GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }




}//class
