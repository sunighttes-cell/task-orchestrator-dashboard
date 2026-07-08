package com.taskOrchestrator.app.auth;
import com.taskOrchestrator.app.auth.domain.User;
import com.taskOrchestrator.app.auth.domain.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldFindUserById() {
        User user = new User();
        user.setFullName("Full Name");
        user.setEmail("test@example.com");
        user.setRole(User.Role.USER);
        user.setProfilePictureUrl("");
        userRepository.save(user);

        Optional<User> foundUser = userRepository.findByUsername(user.getUsername());

        assertTrue(foundUser.isPresent());
        assertNotNull(foundUser.get().getUsername());
        assertEquals("Full Name", foundUser.get().getFullName());
    }

    @Test
    void shouldFindUserByEmail() {
        User user = new User();
        user.setFullName("Full Name");
        user.setEmail("test@example.com");
        user.setRole(User.Role.USER);
        user.setProfilePictureUrl("");
        userRepository.save(user);

        Optional<User> foundUser = userRepository.findByEmail(user.getEmail());

        assertTrue(foundUser.isPresent());
        assertNotNull(foundUser.get().getEmail());
        assertEquals("test@example.com", foundUser.get().getFullName());
    }

    @Test
    void shouldReturnIfUserExistByUsername() {
        User user = new User();
        user.setFullName("Full Name");
        user.setEmail("test@example.com");
        user.setRole(User.Role.USER);
        user.setProfilePictureUrl("");
        userRepository.save(user);

        boolean foundByUsername = userRepository.existsByUsername(user.getUsername());
        assertTrue(foundByUsername);
    }

    @Test
    void shouldReturnIfUserExistByEmail() {
        User user = new User();
        user.setFullName("Full Name");
        user.setEmail("test@example.com");
        user.setRole(User.Role.USER);
        user.setProfilePictureUrl("");
        userRepository.save(user);

        boolean foundByEmail = userRepository.existsByUsername(user.getEmail());
        assertTrue(foundByEmail);
    }

    @Test
    void shouldReturnNullWhenUserNotFound() {
        Optional<User> foundUser = userRepository.findByUsername("nonexistentuser");
        assertFalse(foundUser.isPresent());
    }

    @Test
    void shouldReturnNullWhenEmailNotFound() {
        Optional<User> foundUser = userRepository.findByEmail("nonexistentemail@example.com");
        assertFalse(foundUser.isPresent());
    }

    @Test
    void shouldReturnFalseWhenUserDoesNotExist() {
        boolean foundByUsername = userRepository.existsByUsername("nonexistentuser");
        assertFalse(foundByUsername);
    }

    @Test
    void shouldReturnFalseWhenEmailDoesNotExist() {
        boolean foundByEmail = userRepository.existsByUsername("nonexistentemail@example.com");
        assertFalse(foundByEmail);
    }
}