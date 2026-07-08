package com.taskOrchestrator.app.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskOrchestrator.app.auth.infrastructure.jwt.JwtAuthFilter;
import com.taskOrchestrator.app.auth.infrastructure.jwt.JwtUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    public SecurityConfig(JwtUtil jwtUtil, ObjectMapper objectMapper) {
        this.jwtUtil = jwtUtil;
        this.objectMapper = objectMapper;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors().and().csrf().disable()
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/uploads/**").permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(eh -> eh
                        .authenticationEntryPoint(unauthorizedEntryPoint())
                        .accessDeniedHandler(forbiddenAccessDeniedHandler())
                )
                .addFilterBefore(new JwtAuthFilter(jwtUtil, objectMapper),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // Returns 401 (not Spring Security's default 403) when a protected endpoint is hit without auth.
    private AuthenticationEntryPoint unauthorizedEntryPoint() {
        return (request, response, ex) -> {
            if (response.isCommitted()) {
                return;
            }
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("timestamp", Instant.now().toString());
            body.put("message", "Authentication required");
            body.put("code", "UNAUTHORIZED");
            objectMapper.writeValue(response.getWriter(), body);
        };
    }

    // Returns 403 with a JSON body when an authenticated user lacks the required role.
    private AccessDeniedHandler forbiddenAccessDeniedHandler() {
        return (request, response, ex) -> {
            if (response.isCommitted()) {
                return;
            }
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("timestamp", Instant.now().toString());
            body.put("message", "Access denied");
            body.put("code", "FORBIDDEN");
            objectMapper.writeValue(response.getWriter(), body);
        };
    }
}
