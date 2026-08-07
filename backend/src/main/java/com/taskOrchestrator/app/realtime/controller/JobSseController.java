//Handles HTTP connection
package com.taskOrchestrator.app.realtime.controller;

import com.taskOrchestrator.app.auth.application.CurrentUser;
import com.taskOrchestrator.app.auth.application.CurrentUserProvider;
import com.taskOrchestrator.app.realtime.service.JobEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/realtime")
@RequiredArgsConstructor
public class JobSseController {
    private final JobEventPublisher jobEventPublisher;
    private final CurrentUserProvider currentUserProvider;
    private static final Logger log = LoggerFactory.getLogger(JobSseController.class);

    @GetMapping(value = "/jobs", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamJobEvents() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName() == null || "anonymousUser".equals(authentication.getName())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }

        log.info("SSE authentication: {}", authentication);
        log.info("SSE authenticated: {}", authentication != null && authentication.isAuthenticated());
        log.info("SSE principal: {}", authentication != null ? authentication.getName() : null);
        CurrentUser currentUser = currentUserProvider.getCurrentUser();
        return jobEventPublisher.subscribe(currentUser.username());
    }
}