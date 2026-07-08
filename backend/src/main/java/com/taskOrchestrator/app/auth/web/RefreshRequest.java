package com.taskOrchestrator.app.auth.web;

public record RefreshRequest(
        String refreshToken
) {
    public RefreshRequest {
        if (refreshToken == null) {
            throw new IllegalArgumentException("Refresh token cannot be null");
        }
    }

    public String refreshToken() {
        return refreshToken;
    }
}