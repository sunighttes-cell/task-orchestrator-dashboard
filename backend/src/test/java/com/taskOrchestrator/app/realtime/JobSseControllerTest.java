package com.taskOrchestrator.app.realtime.controller;

import com.taskOrchestrator.app.auth.application.CurrentUser;
import com.taskOrchestrator.app.auth.application.CurrentUserProvider;
import com.taskOrchestrator.app.auth.domain.User;
import com.taskOrchestrator.app.realtime.service.JobEventPublisher;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JobSseControllerTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void streamJobEvents_returnsEmitter_forAuthenticatedUser() {
        JobEventPublisher jobEventPublisher = mock(JobEventPublisher.class);
        CurrentUserProvider currentUserProvider = mock(CurrentUserProvider.class);
        JobSseController controller = new JobSseController(jobEventPublisher, currentUserProvider);

        SseEmitter expected = new SseEmitter();
        when(currentUserProvider.getCurrentUser()).thenReturn(new CurrentUser("alice", User.Role.USER));
        when(jobEventPublisher.subscribe("alice")).thenReturn(expected);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "alice",
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                )
        );

        assertSame(expected, controller.streamJobEvents());
    }

    @Test
    void streamJobEvents_rejectsAnonymousUser() {
        JobEventPublisher jobEventPublisher = mock(JobEventPublisher.class);
        CurrentUserProvider currentUserProvider = mock(CurrentUserProvider.class);
        JobSseController controller = new JobSseController(jobEventPublisher, currentUserProvider);

        SecurityContextHolder.clearContext();

        assertThrows(ResponseStatusException.class, controller::streamJobEvents);
    }
}
