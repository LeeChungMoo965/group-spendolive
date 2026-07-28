

/*package com.example.spendolive.common.security;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // API 및 Ajax 통신을 위해 
            .authorizeHttpRequests(auth -> auth
                // 정적 자원 및 로그인 관련 페이지는 누구나 접근 허용
                .requestMatchers("/css/**", "/js/**", "/images/**", "/member/loginForm.do", "/member/login.do").permitAll()
                // 결제 및 방 관련 주요 기능은 반드시 로그인 필요
                .requestMatchers("/mypage/**", "/ott/**").authenticated()
                // 그 외 모든 요청도 인증 필요
                .anyRequest().authenticated()
            )
            .formLogin(login -> login
                .loginPage("/member/loginForm.do")
                .loginProcessingUrl("/member/login.do")
                .defaultSuccessUrl("/", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/member/logout.do")
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
            );

        return http.build();
    }
} */