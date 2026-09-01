package com.taskOrchestrator.app.auth.infrastructure.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskOrchestrator.app.auth.domain.User;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {
    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private FilterChain filterChain;

    private ObjectMapper objectMapper;
    private JwtAuthFilter jwtAuthFilter;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        jwtAuthFilter = new JwtAuthFilter(
                jwtUtil,
                objectMapper
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldAuthenticateValidToken() throws Exception {

        String token = "valid-token";

        given(jwtUtil.extractUsername(token))
                .willReturn("testuser");

        given(jwtUtil.extractRole(token))
                .willReturn(User.Role.USER);

        given(jwtUtil.isValidAccessToken(token))
                .willReturn(true);

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.setServletPath("/jobs");

        request.addHeader(
                "Authorization",
                "Bearer " + token
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        jwtAuthFilter.doFilter(
                request,
                response,
                filterChain
        );

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        assertThat(authentication)
                .isNotNull();

        assertThat(authentication.getName())
                .isEqualTo("testuser");

        assertThat(authentication.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_USER");

        verify(filterChain)
                .doFilter(request, response);
    }

    @Test
    void shouldAuthenticateAdminToken() throws Exception {

        String token = "admin-token";

        given(jwtUtil.extractUsername(token))
                .willReturn("admin");

        given(jwtUtil.extractRole(token))
                .willReturn(User.Role.ADMIN);

        given(jwtUtil.isValidAccessToken(token))
                .willReturn(true);

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.setServletPath("/jobs");

        request.addHeader(
                "Authorization",
                "Bearer " + token
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        jwtAuthFilter.doFilter(
                request,
                response,
                filterChain
        );

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        assertThat(authentication)
                .isNotNull();

        assertThat(authentication.getName())
                .isEqualTo("admin");

        assertThat(authentication.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_ADMIN");

        verify(filterChain)
                .doFilter(request, response);
    }

    @Test
    void shouldPassThroughWhenAuthorizationHeaderIsMissing()
            throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.setServletPath("/jobs");

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        jwtAuthFilter.doFilter(
                request,
                response,
                filterChain
        );

        assertThat(response.getStatus())
                .isEqualTo(200);

        assertThat(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        ).isNull();

        verifyNoInteractions(jwtUtil);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldPassThroughWhenAuthorizationHeaderIsMalformed()
            throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.setServletPath("/jobs");

        request.addHeader(
                "Authorization",
                "Basic abc123"
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        jwtAuthFilter.doFilter(
                request,
                response,
                filterChain
        );

        assertThat(response.getStatus())
                .isEqualTo(200);

        verifyNoInteractions(jwtUtil);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldReturn401WhenTokenIsInvalid()
            throws Exception {

        String token = "invalid-token";

        given(jwtUtil.extractUsername(token))
                .willThrow(new JwtException("Invalid JWT"));

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.setServletPath("/jobs");

        request.addHeader(
                "Authorization",
                "Bearer " + token
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        jwtAuthFilter.doFilter(
                request,
                response,
                filterChain
        );

        assertThat(response.getStatus())
                .isEqualTo(401);

        assertThat(response.getContentAsString())
                .contains("Invalid token");

        assertThat(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        ).isNull();

        verifyNoInteractions(filterChain);
    }

    @Test
    void shouldReturn401WhenAccessTokenIsExpiredOrInvalid()
            throws Exception {

        String token = "expired-token";

        given(jwtUtil.extractUsername(token))
                .willReturn("testuser");

        given(jwtUtil.extractRole(token))
                .willReturn(User.Role.USER);

        given(jwtUtil.isValidAccessToken(token))
                .willReturn(false);

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.setServletPath("/jobs");

        request.addHeader(
                "Authorization",
                "Bearer " + token
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        jwtAuthFilter.doFilter(
                request,
                response,
                filterChain
        );

        assertThat(response.getStatus())
                .isEqualTo(401);

        assertThat(response.getContentAsString())
                .contains("Invalid token");

        verifyNoInteractions(filterChain);
    }

    @Test
    void shouldSkipAuthenticationEndpoints()
            throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.setServletPath("/auth/login");

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        jwtAuthFilter.doFilter(
                request,
                response,
                filterChain
        );

        verify(filterChain)
                .doFilter(request, response);

        verifyNoInteractions(jwtUtil);
    }

    @Test
    void shouldSkipUploadedFiles() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/uploads/avatar.png");
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtAuthFilter.doFilter(
                request,
                response,
                filterChain
        );

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtUtil);
    }
}