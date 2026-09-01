package com.taskOrchestrator.app.auth;
import com.taskOrchestrator.app.auth.application.AuthService;
import com.taskOrchestrator.app.auth.application.RefreshTokenService;
import com.taskOrchestrator.app.auth.domain.RefreshToken;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

    private AuthService createAuthService() {
        return new AuthService(
                jwtUtil,
                userRepository,
                passwordEncoder,
                refreshTokenService
        );
    }

    private void stubRefreshTokenCreation() {
        when(jwtUtil.extractExpiration(TestData.REFRESH_TOKEN))
                .thenReturn(
                        new Date(System.currentTimeMillis() + 60_000)
                );

        when(refreshTokenService.create(any(User.class), eq(TestData.REFRESH_TOKEN)))
                .thenReturn(new RefreshToken());
    }

    // LOGIN
    @Nested
    class Login {
        @Test
        @DisplayName("Should login successfully")
        void shouldLoginSuccessfully() {
            AuthService authService = createAuthService();
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

            when(jwtUtil.generateRefreshToken(
                    user.getUsername(),
                    user.getRole()
            )).thenReturn(TestData.REFRESH_TOKEN);

            when(refreshTokenService.create(
                    any(User.class),
                    eq(TestData.REFRESH_TOKEN)
            )).thenReturn(new RefreshToken());

            AuthAccessResponse response =
                    authService.login(
                            TestData.USERNAME,
                            TestData.PASSWORD
                    );

            assertNotNull(response);

            assertEquals(
                    TestData.ACCESS_TOKEN,
                    response.getAccessToken()
            );

            assertEquals(
                    TestData.REFRESH_TOKEN,
                    response.getRefreshToken()
            );

            verify(userRepository)
                    .findByUsername(TestData.USERNAME);

            verify(passwordEncoder)
                    .matches(
                            TestData.PASSWORD,
                            user.getPassword()
                    );

            verify(jwtUtil)
                    .generateAccessToken(
                            user.getUsername(),
                            user.getRole()
                    );

            verify(jwtUtil)
                    .generateRefreshToken(
                            user.getUsername(),
                            user.getRole()
                    );

            verify(refreshTokenService)
                    .create(
                            user,
                            TestData.REFRESH_TOKEN
                    );
        }

        @Test
        @DisplayName("Should reject unknown username")
        void shouldRejectUnknownUsername() {

            AuthService authService = createAuthService();

            when(userRepository.findByUsername(TestData.USERNAME))
                    .thenReturn(Optional.empty());

            ResponseStatusException exception =
                    assertThrows(
                            ResponseStatusException.class,
                            () -> authService.login(
                                    TestData.USERNAME,
                                    TestData.PASSWORD
                            )
                    );

            assertEquals(
                    HttpStatus.UNAUTHORIZED,
                    exception.getStatusCode()
            );

            assertEquals(
                    "Invalid username or password",
                    exception.getReason()
            );

            verify(passwordEncoder, never())
                    .matches(any(), any());

            verify(jwtUtil, never())
                    .generateAccessToken(any(), any());

            verify(jwtUtil, never())
                    .generateRefreshToken(any(), any());

            verify(refreshTokenService, never())
                    .create(any(), any());
        }

        @Test
        @DisplayName("Should reject incorrect password")
        void shouldRejectIncorrectPassword() {

            AuthService authService = createAuthService();

            User user = TestUserFactory.encodedUser();

            when(userRepository.findByUsername(TestData.USERNAME))
                    .thenReturn(Optional.of(user));

            when(passwordEncoder.matches(
                    TestData.PASSWORD,
                    user.getPassword()
            )).thenReturn(false);

            ResponseStatusException exception =
                    assertThrows(
                            ResponseStatusException.class,
                            () -> authService.login(
                                    TestData.USERNAME,
                                    TestData.PASSWORD
                            )
                    );

            assertEquals(
                    HttpStatus.UNAUTHORIZED,
                    exception.getStatusCode()
            );

            assertEquals(
                    "Invalid username or password",
                    exception.getReason()
            );

            verify(jwtUtil, never())
                    .generateAccessToken(any(), any());

            verify(jwtUtil, never())
                    .generateRefreshToken(any(), any());

            verify(refreshTokenService, never())
                    .create(any(), any());
        }
    }

    // REFRESH
    @Nested
    class Refresh {

        @Test
        @DisplayName("Should refresh successfully and issue new token pair")
        void shouldRefreshSuccessfullyAndIssueNewTokenPair() {

            AuthService authService = createAuthService();

            User user = TestUserFactory.user();

            RefreshToken storedToken = new RefreshToken();
            storedToken.setUser(user);
            storedToken.setTokenHash("stored-token-hash");

            when(refreshTokenService.validate(
                    TestData.REFRESH_TOKEN
            )).thenReturn(storedToken);

            doNothing()
                    .when(refreshTokenService)
                    .revoke(storedToken);

            when(jwtUtil.generateAccessToken(
                    user.getUsername(),
                    user.getRole()
            )).thenReturn(TestData.ACCESS_TOKEN);

            when(jwtUtil.generateRefreshToken(
                    user.getUsername(),
                    user.getRole()
            )).thenReturn(TestData.REFRESH_TOKEN);

            when(refreshTokenService.create(
                    any(User.class),
                    eq(TestData.REFRESH_TOKEN)
            )).thenReturn(new RefreshToken());

            AuthAccessResponse response =
                    authService.refresh(
                            TestData.REFRESH_TOKEN
                    );

            assertNotNull(response);

            assertEquals(
                    TestData.ACCESS_TOKEN,
                    response.getAccessToken()
            );

            assertEquals(
                    TestData.REFRESH_TOKEN,
                    response.getRefreshToken()
            );

            verify(refreshTokenService)
                    .validate(TestData.REFRESH_TOKEN);

            verify(refreshTokenService)
                    .revoke(storedToken);

            verify(jwtUtil)
                    .generateAccessToken(
                            user.getUsername(),
                            user.getRole()
                    );

            verify(jwtUtil)
                    .generateRefreshToken(
                            user.getUsername(),
                            user.getRole()
                    );

            verify(refreshTokenService)
                    .create(
                            user,
                            TestData.REFRESH_TOKEN
                    );
        }

        @Test
        @DisplayName("Should reject invalid refresh token")
        void shouldRejectInvalidRefreshToken() {

            AuthService authService = createAuthService();

            when(refreshTokenService.validate(
                    TestData.INVALID_REFRESH_TOKEN
            )).thenThrow(
                    new ResponseStatusException(
                            HttpStatus.UNAUTHORIZED,
                            "Invalid refresh token"
                    )
            );

            ResponseStatusException exception =
                    assertThrows(
                            ResponseStatusException.class,
                            () -> authService.refresh(
                                    TestData.INVALID_REFRESH_TOKEN
                            )
                    );

            assertEquals(
                    HttpStatus.UNAUTHORIZED,
                    exception.getStatusCode()
            );

            assertEquals(
                    "Invalid refresh token",
                    exception.getReason()
            );

            verify(refreshTokenService)
                    .validate(TestData.INVALID_REFRESH_TOKEN);

            verify(refreshTokenService, never())
                    .revoke(any());

            verify(jwtUtil, never())
                    .generateAccessToken(any(), any());

            verify(jwtUtil, never())
                    .generateRefreshToken(any(), any());

            verify(refreshTokenService, never())
                    .create(any(), any());
        }

        @Test
        @DisplayName("Should reject expired refresh token")
        void shouldRejectExpiredRefreshToken() {
            AuthService authService = createAuthService();
            when(refreshTokenService.validate(
                    TestData.REFRESH_TOKEN
            )).thenThrow(
                    new ResponseStatusException(
                            HttpStatus.UNAUTHORIZED,
                            "Refresh token has expired"
                    )
            );

            ResponseStatusException exception =
                    assertThrows(
                            ResponseStatusException.class,
                            () -> authService.refresh(
                                    TestData.REFRESH_TOKEN
                            )
                    );

            assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
            assertEquals(
                    "Refresh token has expired",
                    exception.getReason()
            );

            verify(refreshTokenService).validate(TestData.REFRESH_TOKEN);
            verify(refreshTokenService, never()).revoke(any());
            verify(jwtUtil, never()).generateAccessToken(any(), any());
            verify(jwtUtil, never()).generateRefreshToken(any(), any());
        }

        @Test
        @DisplayName("Should reject revoked refresh token")
        void shouldRejectRevokedRefreshToken() {
            AuthService authService = createAuthService();
            when(refreshTokenService.validate(
                    TestData.REFRESH_TOKEN
            )).thenThrow(
                    new ResponseStatusException(
                            HttpStatus.UNAUTHORIZED,
                            "Refresh token has been revoked"
                    )
            );

            ResponseStatusException exception =
                    assertThrows(
                            ResponseStatusException.class,
                            () -> authService.refresh(
                                    TestData.REFRESH_TOKEN
                            )
                    );

            assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
            assertEquals(
                    "Refresh token has been revoked",
                    exception.getReason()
            );

            verify(refreshTokenService).validate(TestData.REFRESH_TOKEN);
            verify(refreshTokenService, never()).revoke(any());
            verify(jwtUtil, never()).generateAccessToken(any(), any());
            verify(jwtUtil, never()).generateRefreshToken(any(), any());
        }
        @Test
        @DisplayName("Should reject refresh when stored token has no user")
        void shouldRejectRefreshWhenStoredTokenHasNoUser() {
            AuthService authService = createAuthService();
            RefreshToken storedToken = new RefreshToken();
            when(refreshTokenService.validate(TestData.REFRESH_TOKEN))
                    .thenReturn(storedToken);

            ResponseStatusException exception =
                    assertThrows(
                            ResponseStatusException.class,
                            () -> authService.refresh(
                                    TestData.REFRESH_TOKEN
                            )
                    );

            assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
            assertEquals(
                    "Invalid refresh token",
                    exception.getReason()
            );

            verify(refreshTokenService).validate(TestData.REFRESH_TOKEN);
            verify(refreshTokenService, never()).revoke(any());
            verify(jwtUtil, never()).generateAccessToken(any(), any());
            verify(jwtUtil, never()).generateAccessToken(any(), any());
            verify(jwtUtil, never()).generateRefreshToken(any(), any());
            verify(refreshTokenService, never()).create(any(), any());
        }
    }

    // REGISTER
    @Nested
    class Register {

        @Test
        @DisplayName("Should register successfully")
        void shouldRegisterSuccessfully() {
            AuthService authService = createAuthService();
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
                    TestData.USERNAME,
                    User.Role.USER
            )).thenReturn(TestData.ACCESS_TOKEN);

            when(jwtUtil.generateRefreshToken(
                    TestData.USERNAME,
                    User.Role.USER
            )).thenReturn(TestData.REFRESH_TOKEN);

            when(refreshTokenService.create(
                    any(User.class),
                    eq(TestData.REFRESH_TOKEN)
            )).thenReturn(new RefreshToken());

            AuthAccessResponse response =
                    authService.register(request);

            assertNotNull(response);

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

            verify(userRepository)
                    .save(captor.capture());

            User savedUser = captor.getValue();

            assertEquals(
                    TestData.USERNAME,
                    savedUser.getUsername()
            );

            assertEquals(TestData.EMAIL, savedUser.getEmail());
            assertEquals(TestData.FULL_NAME, savedUser.getFullName());
            assertEquals("encodedPassword", savedUser.getPassword());
            assertEquals(User.Role.USER, savedUser.getRole());
            verify(passwordEncoder).encode(TestData.PASSWORD);
            verify(jwtUtil).generateAccessToken(TestData.USERNAME, User.Role.USER);
            verify(jwtUtil).generateRefreshToken(TestData.USERNAME, User.Role.USER);
            verify(refreshTokenService).create(any(User.class), eq(TestData.REFRESH_TOKEN));
        }

        @Test
        @DisplayName("Should reject duplicate username")
        void shouldRejectDuplicateUsername() {
            AuthService authService = createAuthService();
            RegisterRequest request =
                    new RegisterRequest(
                            TestData.USERNAME,
                            TestData.EMAIL,
                            TestData.FULL_NAME,
                            TestData.PASSWORD
                    );

            when(userRepository.existsByUsername(
                    TestData.USERNAME
            )).thenReturn(true);

            DuplicateUsernameException exception =
                    assertThrows(
                            DuplicateUsernameException.class,
                            () -> authService.register(request)
                    );

            assertEquals(
                    "Username already exists",
                    exception.getMessage()
            );

            verify(userRepository).existsByUsername(TestData.USERNAME);
            verify(userRepository, never()).existsByEmail(any());
            verify(userRepository, never()).save(any(User.class));
            verify(passwordEncoder, never()).encode(any());
            verify(jwtUtil, never()).generateAccessToken(any(), any());
            verify(jwtUtil, never()).generateRefreshToken(any(), any());
            verify(refreshTokenService, never()).create(any(), any());
        }

        @Test
        @DisplayName("Should reject duplicate email")
        void shouldRejectDuplicateEmail() {
            AuthService authService = createAuthService();
            RegisterRequest request = new RegisterRequest(
                            TestData.USERNAME,
                            TestData.EMAIL,
                            TestData.FULL_NAME,
                            TestData.PASSWORD
            );

            when(userRepository.existsByUsername(TestData.USERNAME
            )).thenReturn(false);

            when(userRepository.existsByEmail(TestData.EMAIL
            )).thenReturn(true);

            DuplicateEmailException exception =
                    assertThrows(
                            DuplicateEmailException.class,
                            () -> authService.register(request)
                    );

            assertEquals(
                    "Email already exists",
                    exception.getMessage()
            );

            verify(userRepository).existsByUsername(TestData.USERNAME);
            verify(userRepository).existsByEmail(TestData.EMAIL);
            verify(userRepository, never()).save(any(User.class));
            verify(passwordEncoder, never()).encode(any());
            verify(jwtUtil, never()).generateAccessToken(any(), any());
            verify(jwtUtil, never()).generateRefreshToken(any(), any());
            verify(refreshTokenService, never()).create(any(), any());
        }
    }

    // LOGOUT
    @Nested
    class Logout {
        @Test
        @DisplayName("Should logout and revoke refresh token")
        void shouldLogoutSuccessfully() {
            AuthService authService = createAuthService();
            User user = TestUserFactory.user();
            RefreshToken storedToken = new RefreshToken();
            storedToken.setUser(user);
            storedToken.setTokenHash("refresh-token-hash");
            when(refreshTokenService.validate(
                    TestData.REFRESH_TOKEN
            )).thenReturn(storedToken);

            doNothing()
                    .when(refreshTokenService)
                    .revoke(storedToken);

            authService.logout(
                    TestData.USERNAME,
                    TestData.REFRESH_TOKEN
            );

            verify(refreshTokenService).validate(TestData.REFRESH_TOKEN);
            verify(refreshTokenService).revoke(storedToken);
        }

        @Test
        @DisplayName("Should reject logout when refresh token belongs to another user")
        void shouldRejectLogoutForDifferentUser() {
            AuthService authService = createAuthService();
            User tokenUser = TestUserFactory.user();
            RefreshToken storedToken = new RefreshToken();
            storedToken.setUser(tokenUser);
            storedToken.setTokenHash("refresh-token-hash");

            when(refreshTokenService.validate(TestData.REFRESH_TOKEN)).thenReturn(storedToken);
            String differentUsername = TestData.USERNAME + "-different";
            ResponseStatusException exception =
                    assertThrows(
                            ResponseStatusException.class,
                            () -> authService.logout(
                                    differentUsername,
                                    TestData.REFRESH_TOKEN
                            )
                    );

            assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
            assertEquals("Invalid refresh token", exception.getReason());
            verify(refreshTokenService).validate(TestData.REFRESH_TOKEN);
            verify(refreshTokenService, never()).revoke(any());
        }

        @Test
        @DisplayName("Should reject logout when refresh token is invalid")
        void shouldRejectLogoutWithInvalidToken() {
            AuthService authService = createAuthService();
            when(refreshTokenService.validate(
                    TestData.INVALID_REFRESH_TOKEN
            )).thenThrow(
                    new ResponseStatusException(
                            HttpStatus.UNAUTHORIZED,
                            "Invalid refresh token"
                    )
            );

            ResponseStatusException exception =
                    assertThrows(
                            ResponseStatusException.class,
                            () -> authService.logout(
                                    TestData.USERNAME,
                                    TestData.INVALID_REFRESH_TOKEN
                            )
                    );

            assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
            assertEquals("Invalid refresh token", exception.getReason());
            verify(refreshTokenService).validate(TestData.INVALID_REFRESH_TOKEN);
            verify(refreshTokenService, never()).revoke(any());
        }
    }

    // LOGOUT ALL
    @Nested
    class LogoutAll {
        @Test
        @DisplayName("Should logout all sessions for user")
        void shouldLogoutAllSessionsForUser() {
            AuthService authService = createAuthService();
            User user = TestUserFactory.user();
            when(userRepository.findByUsername(
                    TestData.USERNAME
            )).thenReturn(Optional.of(user));
            doNothing()
                    .when(refreshTokenService)
                    .revokeAllForUser(user);
            authService.logoutAll(TestData.USERNAME);
            verify(userRepository).findByUsername(TestData.USERNAME);
            verify(refreshTokenService).revokeAllForUser(user);
        }

        @Test
        @DisplayName("Should reject logout all for unknown user")
        void shouldRejectLogoutAllForUnknownUser() {
            AuthService authService = createAuthService();
            when(userRepository.findByUsername(
                    TestData.USERNAME
            )).thenReturn(Optional.empty());
            ResponseStatusException exception =
                    assertThrows(
                            ResponseStatusException.class,
                            () -> authService.logoutAll(
                                    TestData.USERNAME
                            )
                    );

            assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
            assertEquals("User not found", exception.getReason());
            verify(userRepository).findByUsername(TestData.USERNAME);
            verify(refreshTokenService, never()).revokeAllForUser(any());
        }
    }
}