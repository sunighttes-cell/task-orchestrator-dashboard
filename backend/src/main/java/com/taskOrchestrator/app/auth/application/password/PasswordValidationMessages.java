package com.taskOrchestrator.app.auth.application.password;

public final class PasswordValidationMessages {

    private PasswordValidationMessages() {}

    public static final String PASSWORD_REQUIRED =
            "Password is required.";

    public static final String MIN_LENGTH =
            "Password must be at least 8 characters.";

    public static final String MAX_LENGTH =
            "Password cannot exceed 50 characters.";

    public static final String UPPERCASE_REQUIRED =
            "Password must contain an uppercase letter.";

    public static final String LOWERCASE_REQUIRED =
            "Password must contain a lowercase letter.";

    public static final String NUMBER_REQUIRED =
            "Password must contain a number.";

    public static final String SPECIAL_REQUIRED =
            "Password must contain a special character.";

    public static final String CURRENT_PASSWORD_INVALID =
            "Current password is incorrect.";

    public static final String SAME_PASSWORD =
            "New password must be different from your current password.";

    public static final String TOO_SIMILAR =
            "New password is too similar to your current password.";
}