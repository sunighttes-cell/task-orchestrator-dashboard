package com.taskOrchestrator.app.auth.infrastructure;

import com.taskOrchestrator.app.auth.domain.User;
import com.taskOrchestrator.app.auth.domain.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

// Seeds a baseline admin so role-based access can be exercised out of the box.
// Regular users continue to auto-register via AuthService.login.
@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private void createUser(
            String username,
            String fullName,
            String email,
            String password,
            User.Role role
    ) {
        if (userRepository.existsByUsername(username)) {
            return;
        }

        User user = new User();

        user.setUsername(username);
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);

        userRepository.save(user);
    }

    @Override
    public void run(String... args) {
            createUser(
                    "sysAdmin",
                    "System Administrator",
                    "admin@taskapp.local",
                    "admin1234",
                    User.Role.ADMIN
            );

            createUser(
                    "aliceJoe",
                    "Alice Joe",
                    "alice@taskapp.local",
                    "password1234",
                    User.Role.USER
            );

            createUser(
                    "bobSmith",
                    "Bob Smith",
                    "bob@taskapp.local",
                    "password1234",
                    User.Role.USER
            );
    }
}
