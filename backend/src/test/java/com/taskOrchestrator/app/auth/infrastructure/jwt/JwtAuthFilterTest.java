package com.taskOrchestrator.app.auth.infrastructure.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskOrchestrator.app.auth.domain.User;
import com.taskOrchestrator.app.auth.domain.UserRepository;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class JwtAuthFilterTest {

    @Test
    void shouldAcceptValidAccessToken() throws Exception {
        JwtUtil jwtUtil = new JwtUtil(
                "0123456789abcdef0123456789abcdef",
                600000,
                86400000,
                600000
        );
        UserRepository userRepository = mock(UserRepository.class);
        JwtAuthFilter filter = new JwtAuthFilter(jwtUtil, new ObjectMapper(), userRepository);

        String token = jwtUtil.generateAccessToken("alice", User.Role.USER);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/realtime/jobs");
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = new MockFilterChain();

        SecurityContextHolder.clearContext();

        filter.doFilter(request, response, chain);

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldRejectMissingAuthorizationHeader() throws Exception {
        JwtUtil jwtUtil = new JwtUtil(
                "0123456789abcdef0123456789abcdef",
                600000,
                86400000,
                600000
        );
        UserRepository userRepository = mock(UserRepository.class);
        JwtAuthFilter filter = new JwtAuthFilter(jwtUtil, new ObjectMapper(), userRepository);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/realtime/jobs");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = new MockFilterChain();

        SecurityContextHolder.clearContext();

        filter.doFilter(request, response, chain);

        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
    }
}
