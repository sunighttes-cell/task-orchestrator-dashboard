package com.taskOrchestrator.app.auth.application;

import com.taskOrchestrator.app.auth.domain.User;
import java.util.UUID;

public record CurrentUser(
        UUID userId,
        String username,
        User.Role role
) {}
