package com.taskOrchestrator.app.auth.application;

import com.taskOrchestrator.app.auth.domain.User;
import com.taskOrchestrator.app.auth.domain.UserRepository;
import com.taskOrchestrator.app.auth.web.AuthAccessResponse;
import com.taskOrchestrator.app.common.exception.DuplicateEmailException;
import com.taskOrchestrator.app.common.exception.DuplicateUsernameException;
import com.taskOrchestrator.app.auth.infrastructure.jwt.JwtUtil;
import com.taskOrchestrator.app.auth.web.RegisterRequest;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            JwtUtil jwtUtil,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthAccessResponse login(
            String username,
            String password
    ) {

        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "Invalid username or password"
                        )
                );

        if (!passwordEncoder.matches(
                password,
                user.getPassword()
        )) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid username or password"
            );
        }

        return createAuthResponse(user);
    }

    public AuthAccessResponse refresh(
            String refreshToken
    ) {

        try {
            if (!jwtUtil.isValidRefreshToken(refreshToken)) {
                throw new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Invalid refresh token"
                );
            }

        } catch (ExpiredJwtException ex) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Refresh token has expired"
            );

        } catch (JwtException | IllegalArgumentException ex) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid refresh token"
            );
        }

        String username =
                jwtUtil.extractUsername(refreshToken);

        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "User not found"
                        )
                );

        /*
         * Rotate both tokens.
         *
         * The old refresh token is only used to authenticate
         * the refresh request. A new refresh token is returned
         * to continue the authentication lifecycle.
         */
        return createAuthResponse(user);
    }

    public AuthAccessResponse register(
            RegisterRequest request
    ) {

        if (userRepository.existsByUsername(
                request.username()
        )) {
            throw new DuplicateUsernameException(
                    "Username already exists"
            );
        }

        if (userRepository.existsByEmail(
                request.email()
        )) {
            throw new DuplicateEmailException(
                    "Email already exists"
            );
        }

        User user = User.builder()
                .username(request.username())
                .fullName(request.fullName())
                .email(request.email())
                .password(
                        passwordEncoder.encode(
                                request.password()
                        )
                )
                .role(User.Role.USER)
                .build();

        userRepository.save(user);

        return createAuthResponse(user);
    }

    private AuthAccessResponse createAuthResponse(
            User user
    ) {

        String accessToken =
                jwtUtil.generateAccessToken(
                        user.getUsername(),
                        user.getRole()
                );

        String refreshToken =
                jwtUtil.generateRefreshToken(
                        user.getUsername(),
                        user.getRole()
                );

        return new AuthAccessResponse(
                accessToken,
                refreshToken
        );
    }
}