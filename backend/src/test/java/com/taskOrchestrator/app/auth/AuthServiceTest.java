package com.taskOrchestrator.app.auth;

import com.taskOrchestrator.app.auth.application.AuthService;
import com.taskOrchestrator.app.auth.application.RefreshTokenService;
import com.taskOrchestrator.app.auth.domain.User;
import com.taskOrchestrator.app.auth.domain.UserRepository;
import com.taskOrchestrator.app.auth.infrastructure.jwt.JwtUtil;
import com.taskOrchestrator.app.auth.web.AuthAccessResponse;
import com.taskOrchestrator.app.auth.web.RegisterRequest;
import com.taskOrchestrator.app.common.exception.DuplicateEmailException;
import com.taskOrchestrator.app.common.exception.DuplicateUsernameException;
import com.taskOrchestrator.app.common.support.TestData;
import com.taskOrchestrator.app.common.support.TestUserFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthService authService;

    @Nested
    class Login {

        @Test
        @DisplayName("Should login successfully")
        void shouldLoginSuccessfully() {

            User user = TestUserFactory.encodedUser();

            when(userRepository.findByUsername(TestData.USERNAME))
                    .thenReturn(Optional.of(user));

            when(passwordEncoder.matches(
                    TestData.PASSWORD,
                    user.getPassword()
            )).thenReturn(true);

            when(jwtUtil.generateAccessToken(
                    user.getUsername(),
                    user.getRole()
            )).thenReturn(TestData.ACCESS_TOKEN);

            when(refreshTokenService.create(user))
                    .thenReturn(TestData.REFRESH_TOKEN);

            AuthAccessResponse response =
                    authService.login(
                            TestData.USERNAME,
                            TestData.PASSWORD
                    );

            assertEquals(
                    TestData.ACCESS_TOKEN,
                    response.getAccessToken()
            );

            assertEquals(
                    TestData.REFRESH_TOKEN,
                    response.getRefreshToken()
            );
        }

        @Test
        @DisplayName("Should throw when username does not exist")
        void shouldThrowWhenUserDoesNotExist() {

            when(userRepository.findByUsername(TestData.USERNAME))
                    .thenReturn(Optional.empty());

            assertThrows(
                    ResponseStatusException.class,
                    () -> authService.login(
                            TestData.USERNAME,
                            TestData.PASSWORD
                    )
            );
        }

        @Test
        @DisplayName("Should throw when password is incorrect")
        void shouldThrowWhenPasswordIncorrect() {

            User user = TestUserFactory.encodedUser();

            when(userRepository.findByUsername(TestData.USERNAME))
                    .thenReturn(Optional.of(user));

            when(passwordEncoder.matches(
                    TestData.PASSWORD,
                    user.getPassword()
            )).thenReturn(false);

            assertThrows(
                    ResponseStatusException.class,
                    () -> authService.login(
                            TestData.USERNAME,
                            TestData.PASSWORD
                    )
            );
        }
    }

    @Nested
    class Refresh {

        @Test
        @DisplayName("Should refresh token")
        void shouldRefreshToken() {

            User user = TestUserFactory.user();

            when(refreshTokenService.rotate(TestData.REFRESH_TOKEN))
                    .thenReturn(new RefreshTokenService.RefreshTokenRotation(
                            user,
                            TestData.REFRESH_TOKEN
                    ));

            when(jwtUtil.generateAccessToken(
                    user.getUsername(),
                    user.getRole()
            )).thenReturn(TestData.ACCESS_TOKEN);

            AuthAccessResponse response =
                    authService.refresh(
                            TestData.REFRESH_TOKEN
                    );

            assertEquals(
                    TestData.ACCESS_TOKEN,
                    response.getAccessToken()
            );

            assertEquals(
                    TestData.REFRESH_TOKEN,
                    response.getRefreshToken()
            );
        }

        @Test
        @DisplayName("Should reject invalid refresh token")
        void shouldRejectInvalidRefreshToken() {

            when(refreshTokenService.rotate(TestData.INVALID_REFRESH_TOKEN))
                    .thenThrow(new ResponseStatusException(
                            HttpStatus.UNAUTHORIZED,
                            "Invalid refresh token"
                    ));

            assertThrows(
                    ResponseStatusException.class,
                    () -> authService.refresh(
                            TestData.INVALID_REFRESH_TOKEN
                    )
            );
        }

        @Test
        @DisplayName("Should reject expired refresh token")
        void shouldRejectExpiredRefreshToken() {

            when(refreshTokenService.rotate(TestData.REFRESH_TOKEN))
                    .thenThrow(new ResponseStatusException(
                            HttpStatus.UNAUTHORIZED,
                            "Refresh token has expired"
                    ));

            assertThrows(
                    ResponseStatusException.class,
                    () -> authService.refresh(
                            TestData.REFRESH_TOKEN
                    )
            );
        }

        @Test
        @DisplayName("Should reject malformed refresh token")
        void shouldRejectMalformedRefreshToken() {

            when(refreshTokenService.rotate(TestData.REFRESH_TOKEN))
                    .thenThrow(new ResponseStatusException(
                            HttpStatus.UNAUTHORIZED,
                            "Invalid refresh token"
                    ));

            assertThrows(
                    ResponseStatusException.class,
                    () -> authService.refresh(
                            TestData.REFRESH_TOKEN
                    )
            );
        }

        @Test
        @DisplayName("Should reject refresh for unknown user")
        void shouldRejectRefreshForUnknownUser() {

            when(refreshTokenService.rotate(TestData.REFRESH_TOKEN))
                    .thenThrow(new ResponseStatusException(
                            HttpStatus.UNAUTHORIZED,
                            "Invalid refresh token"
                    ));

            assertThrows(
                    ResponseStatusException.class,
                    () -> authService.refresh(
                            TestData.REFRESH_TOKEN
                    )
            );
        }
    }

    @Nested
    class Register {

        @Test
        @DisplayName("Should register successfully")
        void shouldRegisterSuccessfully() {

            RegisterRequest request =
                    new RegisterRequest(
                            TestData.USERNAME,
                            TestData.EMAIL,
                            TestData.FULL_NAME,
                            TestData.PASSWORD
                    );

            when(userRepository.existsByUsername(
                    TestData.USERNAME
            )).thenReturn(false);

            when(userRepository.existsByEmail(
                    TestData.EMAIL
            )).thenReturn(false);

            when(passwordEncoder.encode(
                    TestData.PASSWORD
            )).thenReturn("encodedPassword");

            when(jwtUtil.generateAccessToken(
                    any(),
                    any()
            )).thenReturn(TestData.ACCESS_TOKEN);

            when(refreshTokenService.create(any(User.class)))
                    .thenReturn(TestData.REFRESH_TOKEN);

            AuthAccessResponse response =
                    authService.register(request);

            assertEquals(
                    TestData.ACCESS_TOKEN,
                    response.getAccessToken()
            );

            assertEquals(
                    TestData.REFRESH_TOKEN,
                    response.getRefreshToken()
            );

            ArgumentCaptor<User> captor =
                    ArgumentCaptor.forClass(User.class);

            verify(userRepository).save(captor.capture());

            User saved = captor.getValue();

            assertEquals(
                    TestData.USERNAME,
                    saved.getUsername()
            );

            assertEquals(
                    TestData.EMAIL,
                    saved.getEmail()
            );

            assertEquals(
                    TestData.FULL_NAME,
                    saved.getFullName()
            );

            assertEquals(
                    "encodedPassword",
                    saved.getPassword()
            );

            assertEquals(
                    User.Role.USER,
                    saved.getRole()
            );
        }

        @Test
        @DisplayName("Should reject duplicate username")
        void shouldRejectDuplicateUsername() {

            when(userRepository.existsByUsername(
                    TestData.USERNAME
            )).thenReturn(true);

            RegisterRequest request =
                    new RegisterRequest(
                            TestData.USERNAME,
                            TestData.EMAIL,
                            TestData.FULL_NAME,
                            TestData.PASSWORD
                    );

            assertThrows(
                    DuplicateUsernameException.class,
                    () -> authService.register(request)
            );

            verify(userRepository, never())
                    .save(any(User.class));
        }

        @Test
        @DisplayName("Should reject duplicate email")
        void shouldRejectDuplicateEmail() {

            when(userRepository.existsByUsername(
                    TestData.USERNAME
            )).thenReturn(false);

            when(userRepository.existsByEmail(
                    TestData.EMAIL
            )).thenReturn(true);

            RegisterRequest request =
                    new RegisterRequest(
                            TestData.USERNAME,
                            TestData.EMAIL,
                            TestData.FULL_NAME,
                            TestData.PASSWORD
                    );

            assertThrows(
                    DuplicateEmailException.class,
                    () -> authService.register(request)
            );

            verify(userRepository, never())
                    .save(any(User.class));
        }
    }
}
