package com.taskOrchestrator.app.profile.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UpdateProfileRequest (
        @NotBlank
        @Email
        String email,

        @NotBlank
        String fullName
) {}

