package com.taskOrchestrator.app.auth.web;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank
        @Size(min = 5, max = 255)
        String username,

        @NotBlank
        @Email
        String email,

        @NotBlank
        String fullName,

        @NotBlank
        @Size(min = 5, max = 50)
        String password
) {}
