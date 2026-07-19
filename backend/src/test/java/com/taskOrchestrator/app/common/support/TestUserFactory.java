package com.taskOrchestrator.app.common.support;
import com.taskOrchestrator.app.auth.domain.User;

public final class TestUserFactory {

    private TestUserFactory() {
        // Utility class
    }

    //Standard valid user.
    public static User user() {
        return User.builder()
                .username(TestData.USERNAME)
                .email(TestData.EMAIL)
                .fullName(TestData.FULL_NAME)
                .password(TestData.PASSWORD)
                .profilePictureUrl(null)
                .role(User.Role.USER)
                .build();
    }

    /**User with an already encoded password. Useful for AuthService login tests where PasswordEncoder.matches() is mocked.*/
    public static User encodedUser() {
        return User.builder()
                .username(TestData.USERNAME)
                .email(TestData.EMAIL)
                .fullName(TestData.FULL_NAME)
                .password("$2a$10$EncodedPasswordPlaceholder")
                .profilePictureUrl(null)
                .role(User.Role.USER)
                .build();
    }

    //Administrator user
    public static User admin() {
        return User.builder()
                .username("admin")
                .email("admin@example.com")
                .fullName("Administrator")
                .password("$2a$10$EncodedPasswordPlaceholder")
                .profilePictureUrl(null)
                .role(User.Role.ADMIN)
                .build();
    }

    //Second valid user. Useful when testing duplicate usernames/emails.
    public static User secondUser() {
        return User.builder()
                .username(TestData.USERNAME_TWO)
                .email(TestData.EMAIL_TWO)
                .fullName(TestData.FULL_NAME_TWO)
                .password(TestData.PASSWORD_TWO)
                .profilePictureUrl(null)
                .role(User.Role.USER)
                .build();
    }

    //Creates a user with custom values for edge-case tests.
    public static User user(
            String username,
            String email,
            String fullName,
            String password,
            User.Role role) {

        return User.builder()
                .username(username)
                .email(email)
                .fullName(fullName)
                .password(password)
                .profilePictureUrl(null)
                .role(role)
                .build();
    }
}