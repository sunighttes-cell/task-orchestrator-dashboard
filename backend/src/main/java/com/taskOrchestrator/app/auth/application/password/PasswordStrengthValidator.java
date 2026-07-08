package com.taskOrchestrator.app.auth.application.password;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class PasswordStrengthValidator {

    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 50;

    private static final Pattern UPPERCASE =
            Pattern.compile(".*[A-Z].*");

    private static final Pattern LOWERCASE =
            Pattern.compile(".*[a-z].*");

    private static final Pattern DIGIT =
            Pattern.compile(".*\\d.*");

    private static final Pattern SPECIAL =
            Pattern.compile(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*");

    public void validate(String password) {

        if (password == null || password.isBlank()) {
            throw new PasswordValidationException(
                    PasswordValidationMessages.PASSWORD_REQUIRED);
        }

        if (password.length() < MIN_LENGTH) {
            throw new PasswordValidationException(
                    PasswordValidationMessages.MIN_LENGTH);
        }

        if (password.length() > MAX_LENGTH) {
            throw new PasswordValidationException(
                    PasswordValidationMessages.MAX_LENGTH);
        }

        if (!UPPERCASE.matcher(password).matches()) {
            throw new PasswordValidationException(
                    PasswordValidationMessages.UPPERCASE_REQUIRED);
        }

        if (!LOWERCASE.matcher(password).matches()) {
            throw new PasswordValidationException(
                    PasswordValidationMessages.LOWERCASE_REQUIRED);
        }

        if (!DIGIT.matcher(password).matches()) {
            throw new PasswordValidationException(
                    PasswordValidationMessages.NUMBER_REQUIRED);
        }

        if (!SPECIAL.matcher(password).matches()) {
            throw new PasswordValidationException(
                    PasswordValidationMessages.SPECIAL_REQUIRED);
        }
    }
}