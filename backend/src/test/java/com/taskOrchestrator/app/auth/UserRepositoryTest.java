package com.taskOrchestrator.app.auth;

import com.taskOrchestrator.app.auth.domain.User;
import com.taskOrchestrator.app.auth.domain.UserRepository;
import com.taskOrchestrator.app.common.support.TestUserFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Should save a user")
    void shouldSaveUser() {

        User savedUser = userRepository.save(TestUserFactory.user());

        assertNotNull(savedUser.getId());
        assertEquals("testuser", savedUser.getUsername());
        assertEquals("testuser@example.com", savedUser.getEmail());
        assertEquals("Test User", savedUser.getFullName());
        assertEquals(User.Role.USER, savedUser.getRole());
    }

    @Test
    @DisplayName("Should find user by username")
    void shouldFindUserByUsername() {

        User user = userRepository.save(TestUserFactory.user());

        Optional<User> result =
                userRepository.findByUsername(user.getUsername());

        assertTrue(result.isPresent());
        assertEquals(user.getUsername(), result.get().getUsername());
    }

    @Test
    @DisplayName("Should find user by email")
    void shouldFindUserByEmail() {

        User user = userRepository.save(TestUserFactory.user());

        Optional<User> result =
                userRepository.findByEmail(user.getEmail());

        assertTrue(result.isPresent());
        assertEquals(user.getEmail(), result.get().getEmail());
    }

    @Test
    @DisplayName("Should return empty when username does not exist")
    void shouldReturnEmptyWhenUsernameDoesNotExist() {

        Optional<User> result =
                userRepository.findByUsername("unknown");

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should return empty when email does not exist")
    void shouldReturnEmptyWhenEmailDoesNotExist() {

        Optional<User> result =
                userRepository.findByEmail("unknown@test.com");

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should return true when username exists")
    void shouldReturnTrueWhenUsernameExists() {

        User user = userRepository.save(TestUserFactory.user());

        assertTrue(
                userRepository.existsByUsername(user.getUsername())
        );
    }

    @Test
    @DisplayName("Should return false when username does not exist")
    void shouldReturnFalseWhenUsernameDoesNotExist() {

        assertFalse(
                userRepository.existsByUsername("does-not-exist")
        );
    }

    @Test
    @DisplayName("Should return true when email exists")
    void shouldReturnTrueWhenEmailExists() {

        User user = userRepository.save(TestUserFactory.user());

        assertTrue(
                userRepository.existsByEmail(user.getEmail())
        );
    }

    @Test
    @DisplayName("Should return false when email does not exist")
    void shouldReturnFalseWhenEmailDoesNotExist() {

        assertFalse(
                userRepository.existsByEmail("unknown@test.com")
        );
    }

    @Test
    @DisplayName("Should not allow duplicate usernames")
    void shouldNotAllowDuplicateUsername() {

        userRepository.save(TestUserFactory.user());

        User duplicate =
                TestUserFactory.secondUser();

        duplicate.setUsername("testuser");

        assertThrows(
                DataIntegrityViolationException.class,
                () -> {
                    userRepository.saveAndFlush(duplicate);
                }
        );
    }

    @Test
    @DisplayName("Should not allow duplicate emails")
    void shouldNotAllowDuplicateEmail() {

        userRepository.save(TestUserFactory.user());

        User duplicate =
                TestUserFactory.secondUser();

        duplicate.setEmail("testuser@example.com");

        assertThrows(
                DataIntegrityViolationException.class,
                () -> {
                    userRepository.saveAndFlush(duplicate);
                }
        );
    }
}