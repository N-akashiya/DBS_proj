package com.dbs.tpc_benchmark.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())  // 禁用CSRF
            .cors(cors -> cors.disable())  // 禁用CORS
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()  // 允许所有请求，认证交给JWT拦截器
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 无状态会话
            )
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.disable()) // 允许iframe
            );
        
        return http.build();
    }
}