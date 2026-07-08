package com.taskOrchestrator.app.auth.application.password;

public class PasswordValidationException
        extends RuntimeException {

    public PasswordValidationException(String message) {
        super(message);
    }
}