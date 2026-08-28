package com.taskOrchestrator.app.auth.infrastructure.jwt;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.UUID;
import com.taskOrchestrator.app.auth.domain.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

// Token generation and validation.
@Component
public class JwtUtil {
    private final Key key;
    private final long accessExpiration;
    private final long refreshExpiration;

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-expiration}") long accessExpiration,
            @Value("${jwt.refresh-expiration}") long refreshExpiration
    ) {

        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "JWT_SECRET must be configured"
            );
        }

        this.key = Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpiration = accessExpiration;
        this.refreshExpiration = refreshExpiration;
    }

    public String generateAccessToken(String username, User.Role role) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + accessExpiration);

        return Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setSubject(username)
                .claim("role", role.name())
                .claim("type", "access")
                .setIssuedAt(now)
                .setExpiration(expirationDate)
                .signWith(
                        key,
                        SignatureAlgorithm.HS256
                )
                .compact();
    }

    public String generateRefreshToken(String username, User.Role role) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + refreshExpiration);

        return Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setSubject(username)
                .claim("role", role.name())
                .claim("type", "refresh")
                .setIssuedAt(now)
                .setExpiration(expirationDate)
                .signWith(
                        key,
                        SignatureAlgorithm.HS256
                )
                .compact();
    }

    public String extractUsername(String token) {
        return parse(token).getSubject();
    }

    public String extractClaim(String token) {
        return parse(token)
                .get("type")
                .toString();
    }

    public User.Role extractRole(String token) {
        Object role = parse(token).get("role");

        if (role == null) {
            return User.Role.USER;
        }

        return User.Role.valueOf(role.toString());
    }


    //Extracts the expiration date from a JWT.
    public Date extractExpiration(String token) {
        return parse(token).getExpiration();
    }

    //Validates the JWT signature and expiration.
    public boolean isValid(String token) {
        parse(token);
        return true;
    }

    //Validates that the JWT is both valid and specifically a refresh token.
    public boolean isValidRefreshToken(String refreshToken) {
        return isValid(refreshToken) && "refresh".equals(
                extractClaim(refreshToken)
        );
    }

    //Validates that the JWT is both valid and specifically an access token.
    public boolean isValidAccessToken(String accessToken) {
        return isValid(accessToken) && "access".equals(
                extractClaim(accessToken)
        );
    }

    private Claims parse(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}