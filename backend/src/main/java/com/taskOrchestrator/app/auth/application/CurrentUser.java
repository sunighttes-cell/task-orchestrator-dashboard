package com.taskOrchestrator.app.auth.application;

import com.taskOrchestrator.app.auth.domain.User;

public record CurrentUser(
        String username,
        User.Role role
) {}
