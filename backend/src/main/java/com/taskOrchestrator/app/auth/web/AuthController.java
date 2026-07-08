package com.taskOrchestrator.app.auth.web;
import com.taskOrchestrator.app.auth.application.AuthService;
import org.springframework.web.bind.annotation.*;


//api interface
//Keep this separate because HTTP is just one interface. Later we might add:
//GraphQL, gRPC, CLI //Your application layer shouldn’t care.
@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public AuthAccessResponse login(@RequestBody LoginRequest request) {
        return authService.login(
                request.username(),
                request.password()
        );
    }

    @PostMapping("/refresh")
    public AuthAccessResponse refresh(@RequestBody RefreshRequest request) {
        return authService.refresh(
                request.refreshToken()
        );
    }

    @PostMapping("/register")
    public AuthAccessResponse register(@RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    record RefreshRequest(String refreshToken) {}
    record LoginRequest(String username, String password) {}
}

