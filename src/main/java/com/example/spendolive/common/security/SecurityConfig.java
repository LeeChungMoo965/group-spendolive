package com.example.spendolive.common.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            
            // ★ 1. 브라우저 기본 팝업창(HTTP Basic) 끄기
            .httpBasic(httpBasic -> httpBasic.disable())
            
            // ★ 2. 시큐리티 기본 폼 로그인 끄기 (우리가 만든 컨트롤러 쓰니까)
            .formLogin(formLogin -> formLogin.disable())
            
            // ★ 3. 허용할 URL 및 정적 자원 설정
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/spendolive/admin/**").hasRole("ADMIN")
                .anyRequest().permitAll()
            )
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint((request, response, authException) -> {
                    response.sendRedirect("/spendolive/main.do");
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.sendRedirect("/spendolive/main.do");
                })
            );
        return http.build();
    }
} 