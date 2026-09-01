package com.taskOrchestrator.app.auth.web;

import com.taskOrchestrator.app.auth.web.AuthAccessResponse;
import com.taskOrchestrator.app.auth.application.AuthService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.core.Authentication;
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
            @Valid @RequestBody LoginRequest request) {
        return authService.login(
                request.username(),
                request.password()
        );
    }

    @PostMapping("/refresh")
    public AuthAccessResponse refresh(
            @Valid @RequestBody RefreshRequest request) {
        return authService.refresh(
                request.refreshToken()
        );
    }

    @PostMapping("/register")
    public AuthAccessResponse register(
            @Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

     //Logout the current session.The access token identifies the authenticated user.
     //The refresh token identifies the refresh-token session that should be revoked.
    @PostMapping("/logout")
    public void logout(Authentication authentication,
            @Valid @RequestBody LogoutRequest request) {
        authService.logout(
                authentication.getName(),
                request.refreshToken()
        );
    }

    //Logout all sessions belonging to the authenticated user.
    @PostMapping("/logout-all")
    public void logoutAll(Authentication authentication) {
        authService.logoutAll(authentication.getName());
    }

    public record LoginRequest(
            @NotBlank
            String username,
            @NotBlank
            String password) {
    }

    public record RefreshRequest(
            @NotBlank
            String refreshToken) {
    }

    public record LogoutRequest(
            @NotBlank
            String refreshToken) {
    }
}