package com.taskOrchestrator.app.config;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskOrchestrator.app.auth.infrastructure.jwt.JwtAuthFilter;
import com.taskOrchestrator.app.auth.infrastructure.jwt.JwtUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
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
@EnableWebSecurity
//@EnableMethodSecurity(securedEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtAuthFilter jwtAuthFilter(JwtUtil jwtUtil, ObjectMapper objectMapper) {
        return new JwtAuthFilter(jwtUtil, objectMapper);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter) throws Exception {
        return http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(Customizer.withDefaults())
            .sessionManagement(session ->
                    session.sessionCreationPolicy(
                            SessionCreationPolicy.STATELESS
                    )
            )
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(
                            HttpMethod.OPTIONS,
                            "/**"
                    ).permitAll()
                    .requestMatchers(
                            "/auth/login",
                            "/auth/register",
                            "/auth/refresh"
                    ).permitAll()
                    .anyRequest().authenticated()
            )
            .addFilterBefore(
                    jwtAuthFilter,
                    UsernamePasswordAuthenticationFilter.class
            )
            .build();
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
