package com.taskOrchestrator.app.auth;
import com.taskOrchestrator.app.auth.application.AuthService;
import com.taskOrchestrator.app.auth.domain.User;
import com.taskOrchestrator.app.auth.domain.UserRepository;
import com.taskOrchestrator.app.auth.web.AuthAccessResponse;
import com.taskOrchestrator.app.auth.web.RegisterRequest;
import com.taskOrchestrator.app.common.exception.DuplicateResourceException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthService authService;

    @Test
    void shouldLoginUserSuccessfully() {
        String username = TestDataConstants.TEST_USERNAME;
        String password = TestDataConstants.TEST_PASSWORD;

        User user = new User();
        when(userRepository.save(any(User.class)))
                .thenReturn(user);

        AuthAccessResponse response = authService.login(username, password);
        assertEquals(TestDataConstants.TEST_ACCESS_TOKEN, response.getAccessToken());
        assertEquals(TestDataConstants.TEST_REFRESH_TOKEN, response.getRefreshToken());
    }

    @Test
    void shouldNotLoginUserWithInvalidCredentials() {
        String username = TestDataConstants.TEST_INVALID_USERNAME;
        String password = TestDataConstants.TEST_INVALID_PASSWORD;

        User user = new User();
        when(userRepository.save(any(User.class)))
                .thenReturn(user);

        AuthAccessResponse response = authService.login(username, password);
        Assertions.assertNull(response.getAccessToken());
        Assertions.assertNull(response.getRefreshToken());
    }

    @Test
    void shouldNotLoginUserWithInvalidUsername() {
        String username = TestDataConstants.TEST_INVALID_USERNAME;
        String password = TestDataConstants.TEST_PASSWORD;

        User user = new User();
        when(userRepository.save(any(User.class)))
                .thenReturn(user);

        AuthAccessResponse response = authService.login(username, password);
        Assertions.assertNull(response.getAccessToken());
        Assertions.assertNull(response.getRefreshToken());
    }

    @Test
    void shouldNotLoginUserWithInvalidPassword() {
        String username = TestDataConstants.TEST_USERNAME;
        String password = TestDataConstants.TEST_INVALID_PASSWORD;

        User user = new User();
        when(userRepository.save(any(User.class)))
                .thenReturn(user);

        AuthAccessResponse response = authService.login(username, password);
        Assertions.assertNull(response.getAccessToken());
        Assertions.assertNull(response.getRefreshToken());
    }

    @Test
    void shouldRefreshTokenSuccessfully() {
        String refreshToken = TestDataConstants.TEST_REFRESH_TOKEN;

        User user = new User();
        when(userRepository.save(any(User.class)))
                .thenReturn(user);

        AuthAccessResponse response = authService.refresh(refreshToken);
        assertEquals(TestDataConstants.TEST_ACCESS_TOKEN, response.getAccessToken());
        assertEquals(TestDataConstants.TEST_REFRESH_TOKEN, response.getRefreshToken());
    }

    @Test
    void shouldNotRefreshTokenWithInvalidToken() {
        String refreshToken = TestDataConstants.TEST_INVALID_REFRESH_TOKEN;
        AuthAccessResponse response = authService.refresh(refreshToken);
        Assertions.assertNull(response.getAccessToken());
        Assertions.assertNull(response.getRefreshToken());
    }

    @Test
    void shouldRegisterUserSuccessfully() {
        String username = TestDataConstants.TEST_USERNAME;
        String email = TestDataConstants.TEST_EMAIL;
        String fullName = TestDataConstants.TEST_FULL_NAME;
        String password = TestDataConstants.TEST_PASSWORD;

        RegisterRequest request = new RegisterRequest(username, email, fullName, password);
        when(userRepository.save(any(User.class)))
                .thenReturn(new User());

        AuthAccessResponse response = authService.register(request);
        assertEquals(TestDataConstants.TEST_ACCESS_TOKEN, response.getAccessToken());
        assertEquals(TestDataConstants.TEST_REFRESH_TOKEN, response.getRefreshToken());
    }

    @Test
    void shouldThrowDuplicateResourceExceptionWithUsername() {
        String username = TestDataConstants.TEST_USERNAME;
        String email = TestDataConstants.TEST_EMAIL_TWO;
        String fullName = TestDataConstants.TEST_FULL_NAME;
        String password = TestDataConstants.TEST_PASSWORD;

        RegisterRequest request = new RegisterRequest(username, email, fullName, password);
        when(userRepository.save(any(User.class)))
                .thenThrow(new DuplicateResourceException("Username already exists"));

        assertThrows(DuplicateResourceException.class, () -> authService.register(request));
    }

    @Test
    void shouldThrowDuplicateResourceExceptionWithEmail() {
        String username = TestDataConstants.TEST_USERNAME_TWO;
        String email = TestDataConstants.TEST_EMAIL;
        String fullName = TestDataConstants.TEST_FULL_NAME;
        String password = TestDataConstants.TEST_PASSWORD;

        RegisterRequest request = new RegisterRequest(username, email, fullName, password);
        when(userRepository.save(any(User.class)))
                .thenThrow(new DuplicateResourceException("Email already exists"));

        assertThrows(DuplicateResourceException.class, () -> authService.register(request));
    }
}