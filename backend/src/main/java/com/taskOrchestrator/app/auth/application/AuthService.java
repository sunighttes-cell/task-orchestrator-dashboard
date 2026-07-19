package com.taskOrchestrator.app.auth.application;

import com.taskOrchestrator.app.auth.domain.User;
import com.taskOrchestrator.app.auth.domain.UserRepository;
import com.taskOrchestrator.app.auth.infrastructure.jwt.JwtUtil;
import com.taskOrchestrator.app.auth.web.AuthAccessResponse;
import com.taskOrchestrator.app.auth.web.RegisterRequest;
import com.taskOrchestrator.app.common.exception.DuplicateEmailException;
import com.taskOrchestrator.app.common.exception.DuplicateUsernameException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

//orchestrates domain + infrastructure
//enables thin controllers, makes logic testable and reusable

@Service
public class AuthService {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    public AuthService(JwtUtil jwtUtil, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Demo-friendly login: existing users must provide the right password; unknown
    // usernames auto-register as a regular USER so the demo flow stays low-friction.
    public AuthAccessResponse login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "Invalid username or password"
                        ));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid username or password"
            );
        }

        String accessToken =
                jwtUtil.generateAccessToken(user.getUsername(), user.getRole());
        String refreshToken =
                jwtUtil.generateRefreshToken(user.getUsername(), user.getRole());
        return new AuthAccessResponse(accessToken, refreshToken);
    }

    public AuthAccessResponse refresh(String refreshToken) {
        try {
            if (!jwtUtil.isValidRefreshToken(refreshToken)) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
            }
        } catch (ExpiredJwtException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token has expired");
        } catch (JwtException | IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
        }
        String username = jwtUtil.extractUsername(refreshToken);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        String newAccessToken = jwtUtil.generateAccessToken(username, user.getRole());
        return new AuthAccessResponse(
                newAccessToken,
                refreshToken
        );
    }

    public AuthAccessResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new DuplicateUsernameException("Username already exists");}

        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateEmailException("Email already exists");}

        User user = User.builder()
                .username(request.username())
                .fullName(request.fullName())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(User.Role.USER)
                .build();

        userRepository.save(user);
        String accessToken =
                jwtUtil.generateAccessToken(user.getUsername(), user.getRole());
        String refreshToken =
                jwtUtil.generateRefreshToken(user.getUsername(), user.getRole());

        return new AuthAccessResponse(accessToken, refreshToken);
    }
}
