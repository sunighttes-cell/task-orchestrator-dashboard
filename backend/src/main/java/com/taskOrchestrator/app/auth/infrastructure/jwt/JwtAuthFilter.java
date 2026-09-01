package com.taskOrchestrator.app.auth.infrastructure.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskOrchestrator.app.auth.domain.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.jsonwebtoken.JwtException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            String username = jwtUtil.extractUsername(token);
            User.Role role = jwtUtil.extractRole(token);

            if (!jwtUtil.isValidAccessToken(token)) {
                SecurityContextHolder.clearContext();

                writeUnauthorized(
                        response,
                        "Invalid token",
                        "INVALID_TOKEN"
                );

                return;
            }

            var authorities = List.of(
                    new SimpleGrantedAuthority(
                            "ROLE_" + role.name()
                    )
            );

            var authentication =
                    new UsernamePasswordAuthenticationToken(
                            username,
                            null,
                            authorities
                    );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);

        } catch (JwtException | IllegalArgumentException ex) {
            SecurityContextHolder.clearContext();
            writeUnauthorized(response,
                    "Invalid token",
                    "INVALID_TOKEN"
            );
        }
    }

    //Authentication endpoints and static uploaded files do not require JWT authentication.
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/auth/")
                || path.startsWith("/uploads/");
    }

    private void writeUnauthorized(
            HttpServletResponse response,
            String message,
            String code
    ) throws IOException {
        if (response.isCommitted()) {
            return;
        }

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(
                "application/json"
        );
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("message", message);
        body.put("code", code);
        objectMapper.writeValue(response.getWriter(), body);
    }
}