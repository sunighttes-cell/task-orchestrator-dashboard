

package com.taskOrchestrator.app.profile.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdatePasswordRequest(
        @NotBlank
        @Size(min = 5, max = 50)
        String currentPassword,

        @NotBlank
        @Size(min = 5, max = 50)
        String newPassword
){}