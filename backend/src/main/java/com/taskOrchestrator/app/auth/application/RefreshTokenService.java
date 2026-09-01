package com.taskOrchestrator.app.auth.application;

import com.taskOrchestrator.app.auth.domain.RefreshToken;
import com.taskOrchestrator.app.auth.domain.RefreshTokenRepository;
import com.taskOrchestrator.app.auth.domain.User;
import com.taskOrchestrator.app.auth.infrastructure.jwt.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;
    private final JwtUtil jwtUtil;

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            @Value("${jwt.refresh-expiration}")
            long refreshExpiration,
            JwtUtil jwtUtil
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshExpiration = refreshExpiration;
        this.jwtUtil = jwtUtil;
    }

    public RefreshToken create(User user, String rawRefreshToken) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setTokenHash(hashToken(rawRefreshToken));
        refreshToken.setCreatedAt(Instant.now());
        refreshToken.setExpiresAt(
                jwtUtil.extractExpiration(rawRefreshToken).toInstant()
        );

        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken validate(String rawRefreshToken) {
        try {
            if (!jwtUtil.isValidRefreshToken(rawRefreshToken)) {
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

        String tokenHash = hashToken(rawRefreshToken);
        RefreshToken storedToken =
                refreshTokenRepository
                        .findByTokenHash(tokenHash)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.UNAUTHORIZED,
                                        "Invalid refresh token"
                                )
                        );

        if (storedToken.getRevokedAt() != null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Refresh token has been revoked"
            );
        }

        if (storedToken.getExpiresAt().isBefore(Instant.now())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Refresh token has expired"
            );
        }

        if (storedToken.getUser() == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid refresh token"
            );
        }
        return storedToken;
    }

    @Transactional
    public String rotate(String rawRefreshToken) {

        // 1. Validate the existing refresh token.
        RefreshToken existingToken = validate(rawRefreshToken);

        // 2. Revoke the old refresh token.
        existingToken.setRevokedAt(Instant.now());
        refreshTokenRepository.save(existingToken);

        // 3. Generate a completely new refresh JWT.
        User user = existingToken.getUser();

        String newRawRefreshToken =
                jwtUtil.generateRefreshToken(
                        user.getUsername(),
                        user.getRole()
                );

        // 4. Hash the new token before storing it.
        String newTokenHash = hashToken(newRawRefreshToken);

        // 5. Create the new database record.
        RefreshToken newRefreshToken = new RefreshToken();
        newRefreshToken.setUser(user);
        newRefreshToken.setTokenHash(newTokenHash);
        newRefreshToken.setCreatedAt(Instant.now());
        newRefreshToken.setExpiresAt(
                Instant.now().plusMillis(refreshExpiration)
        );

        // 6. Persist the new refresh token.
        refreshTokenRepository.save(newRefreshToken);

        // 7. Return the RAW token.
        // The database only receives the hash.
        // The frontend receives the actual JWT.
        return newRawRefreshToken;
    }

    public void revoke(RefreshToken refreshToken) {
        if (refreshToken.getRevokedAt() == null) {
            refreshToken.setRevokedAt(Instant.now());
            refreshTokenRepository.save(refreshToken);
        }
    }

    @Transactional
    public void revokeAllForUser(User user) {
        List<RefreshToken> activeTokens =
                refreshTokenRepository
                        .findByUserAndRevokedAtIsNull(user);
        Instant revokedAt = Instant.now();

        for (RefreshToken token : activeTokens) {
            token.setRevokedAt(revokedAt);
        }
        refreshTokenRepository.saveAll(activeTokens);
    }

    @Transactional(readOnly = true)
    public boolean detectReuse(String rawRefreshToken) {
        String tokenHash = hashToken(rawRefreshToken);
        return refreshTokenRepository
                .findByTokenHash(tokenHash)
                .map(token -> token.getRevokedAt() != null)
                .orElse(false);
    }

    public RefreshToken findByRawToken(String rawToken) {
        String tokenHash = hashToken(rawToken);
        return refreshTokenRepository
                .findByTokenHash(tokenHash)
                .orElse(null);
    }

    public RefreshToken findByRawTokenForReuse(String rawToken) {
        String tokenHash = hashToken(rawToken);
        return refreshTokenRepository
                .findByTokenHash(tokenHash)
                .orElse(null);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                            token.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(
                    "SHA-256 algorithm is not available",
                    ex
            );
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