package com.taskOrchestrator.app.common.controller;

import com.taskOrchestrator.app.auth.domain.User;
import com.taskOrchestrator.app.auth.infrastructure.jwt.JwtUtil;
import com.taskOrchestrator.app.config.SecurityConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import java.util.UUID;

/**Base class for controller tests that require authentication. ControllerTests should extend this class.*/
@WebMvcTest
@Import(SecurityConfig.class)
public abstract class AuthenticatedControllerTest extends BaseControllerTest {
    @MockitoBean
    private JwtUtil jwtUtil;
    protected static final String USERNAME = "testuser";
    protected static final String ADMIN = "admin";
    protected static final UUID USER_ID = UUID.fromString(
            "11111111-1111-1111-1111-111111111111");

    protected String userToken() {
        return jwtUtil.generateAccessToken(
                USERNAME,
                User.Role.USER
        );
    }

    protected String adminToken() {
        return jwtUtil.generateAccessToken(
                ADMIN,
                User.Role.ADMIN
        );
    }

    protected RequestPostProcessor authenticatedUser() {
        return authentication(
                new UsernamePasswordAuthenticationToken(
                        "testuser",
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                )
        );
    }

    //Adds USER Authorization header
    protected MockHttpServletRequestBuilder authenticate(
            MockHttpServletRequestBuilder builder
    ) {
        return builder.header(
                HttpHeaders.AUTHORIZATION,
                "Bearer " + userToken()
        );
    }

    //Adds ADMIN Authorization header
    protected MockHttpServletRequestBuilder authenticateAdmin(
            MockHttpServletRequestBuilder builder
    ) {
        return builder.header(
                HttpHeaders.AUTHORIZATION,
                "Bearer " + adminToken()
        );
    }
}