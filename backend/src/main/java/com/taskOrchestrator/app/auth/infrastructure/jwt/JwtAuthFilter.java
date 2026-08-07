package com.taskOrchestrator.app.auth.infrastructure.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskOrchestrator.app.auth.application.CurrentUser;
import com.taskOrchestrator.app.auth.domain.User;
import com.taskOrchestrator.app.auth.domain.UserRepository;
import com.taskOrchestrator.app.common.exception.UserNotFoundException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

//request interception. Filters run before the DispatcherServlet, so we must
//translate JWT errors to 401 responses here — @RestControllerAdvice cannot catch them.
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;

    public JwtAuthFilter(JwtUtil jwtUtil, ObjectMapper objectMapper, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.objectMapper = objectMapper;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestPath = request.getRequestURI();
        if (requestPath.endsWith("/auth/login")
                || requestPath.endsWith("/auth/register")
                || requestPath.endsWith("/auth/refresh")) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            SecurityContextHolder.clearContext();
            writeUnauthorized(response, "Authentication required", "UNAUTHORIZED");
            return;
        }

        String token = authHeader.substring(7);
        try {
            String username = jwtUtil.extractUsername(token);
            User.Role role = jwtUtil.extractRole(token);
            if (!jwtUtil.isValidAccessToken(token)) {
                SecurityContextHolder.clearContext();
                writeUnauthorized(response, "Invalid token", "INVALID_TOKEN");
                return;
            }

            var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
            var auth = new UsernamePasswordAuthenticationToken(username, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(auth);
        } catch (JwtException | IllegalArgumentException ex) {
            SecurityContextHolder.clearContext();
            writeUnauthorized(response, "Invalid token", "INVALID_TOKEN");
            return;
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();

        return path.startsWith("/auth/") || path.startsWith("/uploads/");
    }

    private void writeUnauthorized(HttpServletResponse response, String message, String code) throws IOException {
        if (response.isCommitted()) {
            return;
        }
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("message", message);
        body.put("code", code);
        objectMapper.writeValue(response.getWriter(), body);
    }
}
