package com.taskOrchestrator.app.common.controller;

import com.taskOrchestrator.app.auth.infrastructure.jwt.JwtUtil;
import com.taskOrchestrator.app.common.exception.GlobalExceptionHandler;
import com.taskOrchestrator.app.config.SecurityConfig;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

@Import({
        SecurityConfig.class,
        GlobalExceptionHandler.class
})
public abstract class AuthenticatedControllerTest
        extends BaseControllerTest {

    @MockitoBean
    protected JwtUtil jwtUtil;

    protected static final String USERNAME = "testuser";

    protected static final String ADMIN = "admin";

    protected static final UUID USER_ID =
            UUID.fromString(
                    "11111111-1111-1111-1111-111111111111"
            );

    protected RequestPostProcessor authenticatedUser() {

        return authentication(
                new UsernamePasswordAuthenticationToken(
                        USERNAME,
                        null,
                        List.of(
                                new SimpleGrantedAuthority(
                                        "ROLE_USER"
                                )
                        )
                )
        );
    }

    protected RequestPostProcessor authenticatedAdmin() {
        return authentication(
                new UsernamePasswordAuthenticationToken(
                        ADMIN,
                        null,
                        List.of(
                                new SimpleGrantedAuthority(
                                        "ROLE_ADMIN"
                                )
                        )
                )
        );
    }
}