package com.taskOrchestrator.app.profile;
import com.taskOrchestrator.app.auth.TestDataConstants;
import com.taskOrchestrator.app.auth.domain.User;
import com.taskOrchestrator.app.auth.domain.UserRepository;
import com.taskOrchestrator.app.common.exception.UserNotFoundException;
import com.taskOrchestrator.app.profile.dto.UpdatePasswordRequest;
import com.taskOrchestrator.app.profile.dto.UpdateProfileRequest;
import com.taskOrchestrator.app.profile.dto.UserProfileResponse;
import com.taskOrchestrator.app.profile.service.UserProfileService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserProfileService userProfileService;

    //update job
    @Test
    void shouldUpdateProfileSuccessfully() {
        String fullName = TestDataConstants.TEST_FULL_NAME;
        String email = TestDataConstants.TEST_EMAIL;

        User user = new User();
        UpdateProfileRequest request = new UpdateProfileRequest(email, fullName);

        when(userRepository.save(any(User.class)))
                .thenReturn(user);

        UserProfileResponse result = userProfileService.updateProfile(request);

        assertEquals(fullName, result.getFullName());
        assertEquals(email, result.getEmail());
    }

    @Test
    void shouldUpdatePasswordSuccessfully() {
        String currentPassword = "passWCurr@1234";
        String newPassword = "newPassword1234!";

        User user = new User();
        UpdatePasswordRequest request = new UpdatePasswordRequest(currentPassword, newPassword);

        when(userRepository.save(any(User.class)))
                .thenReturn(user);

        UserProfileResponse result = userProfileService.updatePassword(request);
        assertEquals(newPassword, result);
    }

    @Test
    void shouldUpdateAvatarSuccessfully() throws IOException {
        String avatarUrl = TestDataConstants.TEST_PROFILE_PICTURE_URL;

        MultipartFile mockMultipartFile = new MockMultipartFile("file",
                "test.png", "image/png", avatarUrl.getBytes());

        User user = new User();
        when(userRepository.save(any(User.class)))
                .thenReturn(user);

        UserProfileResponse result = userProfileService.uploadAvatar(mockMultipartFile);

        //avatarUrl?? or pathname??
        assertEquals(avatarUrl, result.getProfilePictureUrl());
    }

    //edge cases
    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        when(userRepository.findByUsername(TestDataConstants.TEST_USERNAME))
                .thenReturn(java.util.Optional.empty());

        assertThrows(UserNotFoundException.class, () ->
                userProfileService.getUser(TestDataConstants.TEST_USERNAME));

    }
}