package com.taskOrchestrator.app.auth.web;

public record AuthAccessResponse(
        String accessToken,
        String refreshToken
) {
    public AuthAccessResponse {
        if (accessToken == null || refreshToken == null) {
            throw new IllegalArgumentException("Access and refresh tokens cannot be null");
        }
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    @Override
    public String toString() {
        return "AuthResponse{" +
                "accessToken='" + accessToken + '\'' +
                ", refreshToken='" + refreshToken + '\'' +
                '}';
    }
}