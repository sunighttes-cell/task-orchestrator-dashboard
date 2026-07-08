package com.taskOrchestrator.app.auth.application.password;
import com.taskOrchestrator.app.auth.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PasswordPolicyValidator {

    private static final int MAX_SHARED_SEQUENCE = 4;

    private final PasswordEncoder passwordEncoder;
    private final PasswordStrengthValidator strengthValidator;

    public void validate(User user,
                         String currentPassword,
                         String newPassword) {

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new PasswordValidationException(
                    PasswordValidationMessages.CURRENT_PASSWORD_INVALID);
        }

        strengthValidator.validate(newPassword);

        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new PasswordValidationException(
                    PasswordValidationMessages.SAME_PASSWORD);
        }

        validateSimilarity(currentPassword, newPassword);
    }

    private void validateSimilarity(String current,
                                    String replacement) {

        String currentLower = current.toLowerCase();
        String replacementLower = replacement.toLowerCase();

        if (replacementLower.contains(currentLower)
                || currentLower.contains(replacementLower)) {

            throw new PasswordValidationException(
                    PasswordValidationMessages.TOO_SIMILAR);
        }

        for (int length = MAX_SHARED_SEQUENCE;
             length <= currentLower.length();
             length++) {

            for (int start = 0;
                 start <= currentLower.length() - length;
                 start++) {

                String fragment =
                        currentLower.substring(start, start + length);

                if (replacementLower.contains(fragment)) {
                    throw new PasswordValidationException(
                            PasswordValidationMessages.TOO_SIMILAR);
                }
            }
        }
    }
}