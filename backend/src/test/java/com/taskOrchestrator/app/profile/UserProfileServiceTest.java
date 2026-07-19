package com.taskOrchestrator.app.profile;

import com.taskOrchestrator.app.auth.application.CurrentUser;
import com.taskOrchestrator.app.auth.application.CurrentUserProvider;
import com.taskOrchestrator.app.auth.domain.User;
import com.taskOrchestrator.app.auth.domain.UserRepository;
import com.taskOrchestrator.app.profile.dto.UpdatePasswordRequest;
import com.taskOrchestrator.app.profile.dto.UpdateProfileRequest;
import com.taskOrchestrator.app.profile.dto.UserProfileResponse;
import com.taskOrchestrator.app.auth.application.password.PasswordPolicyValidator;
import com.taskOrchestrator.app.profile.service.UserProfileService;
import com.taskOrchestrator.app.profile.storage.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private PasswordPolicyValidator passwordPolicyValidator;

    @InjectMocks
    private UserProfileService userProfileService;

    private User user;
    private CurrentUser currentUser =
            new CurrentUser("testuser", User.Role.USER);

    @BeforeEach
    void setUp() {
        user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .fullName("Test User")
                .password("encoded-password")
                .role(User.Role.USER)
                .build();

        System.out.println("currentUser: " + currentUser);
    }

    @Nested
    class GetUser {

        @Test
        void shouldReturnUserByUsername() {

            given(userRepository.findByUsername("testuser"))
                    .willReturn(Optional.of(user));

            User result =
                    userProfileService.getUser("testuser");

            assertThat(result)
                    .isSameAs(user);

            verify(userRepository)
                    .findByUsername("testuser");
        }

        @Test
        void shouldThrowUnauthorizedWhenUserDoesNotExist() {

            given(userRepository.findByUsername("missing-user"))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() ->
                    userProfileService.getUser("missing-user")
            )
                    .isInstanceOf(ResponseStatusException.class)
                    .satisfies(exception -> {
                        ResponseStatusException responseException =
                                (ResponseStatusException) exception;

                        assertThat(responseException.getStatusCode())
                                .isEqualTo(HttpStatus.UNAUTHORIZED);
                    });
        }
    }

    @Nested
    class GetProfile {
        @Test
        void shouldReturnCurrentUserProfile() {
            given(currentUserProvider.getCurrentUser())
                    .willReturn(currentUser);
            given(userRepository.findByUsername("testuser"))
                    .willReturn(Optional.of(user));
            UserProfileResponse result = userProfileService.getProfile();
            assertThat(result).isNotNull();
            verify(currentUserProvider).getCurrentUser();
            verify(userRepository).findByUsername("testuser");
        }

        @Test
        void shouldThrowUnauthorizedWhenCurrentUserDoesNotExist() {
            given(currentUserProvider.getCurrentUser())
                    .willReturn(currentUser);
            given(userRepository.findByUsername("testuser"))
                    .willReturn(Optional.of(user));
            given(userRepository.findByUsername("testuser"))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() ->
                    userProfileService.getProfile()
            )
                    .isInstanceOf(ResponseStatusException.class)
                    .satisfies(exception -> {
                        ResponseStatusException responseException =
                                (ResponseStatusException) exception;

                        assertThat(responseException.getStatusCode())
                                .isEqualTo(HttpStatus.UNAUTHORIZED);
                    });
        }
    }

    @Nested
    class UpdateProfile {
        @Test
        void shouldUpdateEmailAndFullName() {
            given(currentUserProvider.getCurrentUser())
                    .willReturn(currentUser);
            given(userRepository.findByUsername("testuser"))
                    .willReturn(Optional.of(user));
            UpdateProfileRequest request =
                    new UpdateProfileRequest(
                            "updated@example.com",
                            "Updated User"
                    );

            UserProfileResponse result = userProfileService.updateProfile(request);
            assertThat(user.getEmail()).isEqualTo("updated@example.com");
            assertThat(user.getFullName()).isEqualTo("Updated User");
            verify(userRepository).save(user);
        }

        @Test
        void shouldUpdateOnlyTheAuthenticatedUser() {
            given(currentUserProvider.getCurrentUser())
                    .willReturn(currentUser);
            given(userRepository.findByUsername("testuser"))
                    .willReturn(Optional.of(user));
            UpdateProfileRequest request = new UpdateProfileRequest(
                    "updated@example.com",
                    "Updated User"
            );

            userProfileService.updateProfile(request);
            verify(currentUserProvider).getCurrentUser();
            verify(userRepository).findByUsername("testuser");
            verify(userRepository).save(user);
        }
    }

    @Nested
    class UpdatePassword {
        @Test
        void shouldValidateAndUpdatePassword() {
            given(currentUserProvider.getCurrentUser())
                    .willReturn(currentUser);
            given(userRepository.findByUsername("testuser"))
                    .willReturn(Optional.of(user));
            UpdatePasswordRequest request = new UpdatePasswordRequest(
                    "OldPassword123!",
                    "NewPassword123!"
            );

            given(passwordEncoder.encode("NewPassword123!"))
                    .willReturn("new-encoded-password");

            UserProfileResponse result = userProfileService.updatePassword(request);
            verify(passwordPolicyValidator).validate(
                    user,
                    "OldPassword123!",
                    "NewPassword123!"
            );

            verify(passwordEncoder).encode("NewPassword123!");
            assertThat(user.getPassword()).isEqualTo("new-encoded-password");
            verify(userRepository).saveAndFlush(user);
            assertThat(result).isNotNull();
        }

        @Test
        void shouldNotUpdatePasswordWhenValidationFails() {
            given(currentUserProvider.getCurrentUser())
                    .willReturn(currentUser);
            given(userRepository.findByUsername("testuser"))
                    .willReturn(Optional.of(user));
            UpdatePasswordRequest request = new UpdatePasswordRequest(
                    "WrongPassword123!",
                    "NewPassword123!"
            );

            RuntimeException exception = new RuntimeException("Invalid password");

            org.mockito.Mockito
                    .doThrow(exception)
                    .when(passwordPolicyValidator)
                    .validate(
                            user,
                            "WrongPassword123!",
                            "NewPassword123!");

            assertThatThrownBy(() ->
                    userProfileService.updatePassword(request))
                    .isSameAs(exception);

            verify(passwordPolicyValidator).validate(
                    user, "WrongPassword123!", "NewPassword123!");

            verify(passwordEncoder, never()).encode(any());
            verify(userRepository, never()).saveAndFlush(any());
        }
    }

    @Nested
    class UploadAvatar {
        @Test
        void shouldUploadAndSaveAvatarUrl() throws IOException {
            given(currentUserProvider.getCurrentUser())
                    .willReturn(currentUser);
            given(userRepository.findByUsername("testuser"))
                    .willReturn(Optional.of(user));
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "avatar.png",
                    "image/png",
                    "image-content".getBytes()
            );

            given(fileStorageService.store(file)).willReturn("/uploads/avatar.png");
            UserProfileResponse result = userProfileService.uploadAvatar(file);
            assertThat(user.getProfilePictureUrl()).isEqualTo("/uploads/avatar.png");

            verify(fileStorageService).store(file);
            verify(userRepository).saveAndFlush(user);
            assertThat(result).isNotNull();
        }

        @Test
        void shouldNotSaveUserWhenFileStorageFails() throws IOException {
            given(currentUserProvider.getCurrentUser())
                    .willReturn(currentUser);
            given(userRepository.findByUsername("testuser"))
                    .willReturn(Optional.of(user));
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "avatar.png",
                    "image/png",
                    "image-content".getBytes()
            );

            IOException exception = new IOException("Unable to store file");
            given(fileStorageService.store(file)).willThrow(exception);
            assertThatThrownBy(() ->
                    userProfileService.uploadAvatar(file))
                    .isSameAs(exception);

            verify(fileStorageService).store(file);
            verify(userRepository, never()).saveAndFlush(any());
        }
    }
}