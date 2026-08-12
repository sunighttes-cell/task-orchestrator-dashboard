package com.taskOrchestrator.app.auth.web;

import com.taskOrchestrator.app.auth.web.AuthAccessResponse;
import com.taskOrchestrator.app.auth.application.AuthService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public AuthAccessResponse login(
            @Valid @RequestBody LoginRequest request
    ) {

        return authService.login(
                request.username(),
                request.password()
        );
    }

    @PostMapping("/refresh")
    public AuthAccessResponse refresh(
            @Valid @RequestBody RefreshRequest request
    ) {

        return authService.refresh(
                request.refreshToken()
        );
    }

    @PostMapping("/register")
    public AuthAccessResponse register(
            @Valid @RequestBody RegisterRequest request
    ) {

        return authService.register(request);
    }

    public record LoginRequest(
            @NotBlank
            String username,

            @NotBlank
            String password
    ) {
    }

    public record RefreshRequest(
            @NotBlank
            String refreshToken
    ) {
    }
}