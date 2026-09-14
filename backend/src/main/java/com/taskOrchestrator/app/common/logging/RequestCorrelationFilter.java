package com.taskOrchestrator.app.common.logging;

import com.taskOrchestrator.app.auth.application.CurrentUser;
import com.taskOrchestrator.app.auth.application.CurrentUserProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class RequestCorrelationFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(RequestCorrelationFilter.class);
    private final CurrentUserProvider currentUserProvider;

    public RequestCorrelationFilter(CurrentUserProvider currentUserProvider) {
        this.currentUserProvider = currentUserProvider;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String requestId = request.getHeader(
                LoggingConstants.REQUEST_ID_HEADER
        );

        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }

        long startTime = System.nanoTime();

        try {
            MDC.put(LoggingConstants.REQUEST_ID, requestId);
            MDC.put(LoggingConstants.HTTP_METHOD, request.getMethod());
            MDC.put(LoggingConstants.HTTP_PATH, request.getRequestURI());
            response.setHeader(LoggingConstants.REQUEST_ID_HEADER, requestId);
            log.info("HTTP request started");

            filterChain.doFilter(request, response);
        } finally {
            addAuthenticatedUserToMdc();
            long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
            MDC.put(LoggingConstants.DURATION_MS, String.valueOf(durationMs));
            MDC.put(LoggingConstants.STATUS, String.valueOf(response.getStatus()));
            log.info("HTTP request completed");

            MDC.clear();
        }
    }

    private void addAuthenticatedUserToMdc() {
        try {
            CurrentUser currentUser = currentUserProvider.getCurrentUser();
            MDC.put(LoggingConstants.USER_ID, currentUser.userId().toString());
        } catch (RuntimeException ignored) {
            /*
             * Anonymous/public endpoints such as:
             * /auth/login or /auth/register or /actuator/health
             * do not have an authenticated user.
             * No userId is added to MDC in those cases.
             */
        }
    }
}