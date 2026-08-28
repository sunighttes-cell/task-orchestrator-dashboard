package com.taskOrchestrator.app.auth.application;

import com.taskOrchestrator.app.auth.domain.RefreshToken;
import com.taskOrchestrator.app.auth.domain.RefreshTokenRepository;
import com.taskOrchestrator.app.auth.domain.User;
import com.taskOrchestrator.app.auth.infrastructure.jwt.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.List;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            JwtUtil jwtUtil
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public String create(User user) {

        if (user == null) {
            throw new IllegalArgumentException(
                    "User cannot be null"
            );
        }

        String rawToken =
                jwtUtil.generateRefreshToken(
                        user.getUsername(),
                        user.getRole()
                );

        String tokenHash = hashToken(rawToken);
        Instant now = Instant.now();
        RefreshToken refreshToken = new RefreshToken();

        refreshToken.setUser(user);
        refreshToken.setTokenHash(tokenHash);
        refreshToken.setCreatedAt(now);

        refreshToken.setExpiresAt(
                jwtUtil.extractExpiration(
                        rawToken
                ).toInstant()
        );

        refreshToken.setRevokedAt(null);
        refreshTokenRepository.save(refreshToken);
        return rawToken;
    }

    @Transactional(readOnly = true)
    public RefreshToken validate(String rawToken) {

        if (rawToken == null || rawToken.isBlank()) {
            throw unauthorized(
                    "Refresh token is required"
            );
        }

        try {

            if (!jwtUtil.isValidRefreshToken(rawToken)) {
                throw unauthorized(
                        "Invalid refresh token"
                );
            }

        } catch (ExpiredJwtException ex) {

            throw unauthorized(
                    "Refresh token has expired"
            );

        } catch (JwtException | IllegalArgumentException ex) {

            throw unauthorized(
                    "Invalid refresh token"
            );
        }

        String tokenHash = hashToken(rawToken);

        RefreshToken refreshToken = refreshTokenRepository
                        .findByTokenHash(tokenHash)
                        .orElseThrow(() ->
                                unauthorized("Invalid refresh token")
                        );

        if (refreshToken.getRevokedAt() != null) {

            throw unauthorized(
                    "Refresh token has been revoked"
            );
        }

        if (refreshToken.getExpiresAt()
                .isBefore(Instant.now())) {

            throw unauthorized(
                    "Refresh token has expired"
            );
        }

        return refreshToken;
    }

    @Transactional(noRollbackFor = ResponseStatusException.class)
    public RefreshTokenRotation rotate(
            String rawToken
    ) {

        if (rawToken == null || rawToken.isBlank()) {
            throw unauthorized(
                    "Refresh token is required"
            );
        }

        if (detectReuse(rawToken)) {
            RefreshToken revokedToken = findByRawToken(rawToken);

            if (revokedToken != null) {
                revokeAllForUser(revokedToken.getUser());
            }

            throw unauthorized("Refresh token reuse detected");
        }

        RefreshToken existingToken = validate(rawToken);
        User user = existingToken.getUser();

        revoke(existingToken);

        String newRefreshToken = create(user);

        return new RefreshTokenRotation(
                user,
                newRefreshToken
        );
    }

    @Transactional
    public void revoke(
            RefreshToken refreshToken
    ) {

        if (refreshToken == null) {
            return;
        }

        if (refreshToken.getRevokedAt() != null) {
            return;
        }

        refreshToken.setRevokedAt(Instant.now());

        refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public void revokeAllForUser(
            User user
    ) {

        if (user == null) {
            return;
        }

        List<RefreshToken> tokens =
                refreshTokenRepository.findByUser(user);

        Instant now = Instant.now();

        for (RefreshToken token : tokens) {

            if (token.getRevokedAt() == null) {

                token.setRevokedAt(now);
            }
        }

        refreshTokenRepository.saveAll(tokens);
    }

    @Transactional(readOnly = true)
    public boolean detectReuse(
            String rawToken
    ) {

        if (rawToken == null || rawToken.isBlank()) {
            return false;
        }

        String tokenHash =
                hashToken(rawToken);

        return refreshTokenRepository
                .findByTokenHash(tokenHash)
                .map(token ->
                        token.getRevokedAt() != null
                )
                .orElse(false);
    }

    private RefreshToken findByRawToken(
            String rawToken
    ) {

        String tokenHash =
                hashToken(rawToken);

        return refreshTokenRepository
                .findByTokenHash(tokenHash)
                .orElse(null);
    }

    private String hashToken(String rawToken
    ) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(
                            rawToken.getBytes(StandardCharsets.UTF_8));

            StringBuilder hex = new StringBuilder(hash.length * 2);

            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }

            return hex.toString();

        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(
                    "SHA-256 algorithm is not available", ex);
        }
    }

    private ResponseStatusException unauthorized(String message) {
        return new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                message
        );
    }


    public record RefreshTokenRotation(User user, String refreshToken) {
    }
}