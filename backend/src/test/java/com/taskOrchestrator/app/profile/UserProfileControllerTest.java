package com.taskOrchestrator.app.profile;

import com.taskOrchestrator.app.auth.infrastructure.jwt.JwtAuthFilter;
import com.taskOrchestrator.app.auth.infrastructure.jwt.JwtUtil;
import com.taskOrchestrator.app.common.controller.AuthenticatedControllerTest;
import com.taskOrchestrator.app.common.exception.GlobalExceptionHandler;
import com.taskOrchestrator.app.profile.controller.UserProfileController;
import com.taskOrchestrator.app.profile.service.UserProfileService;
import com.taskOrchestrator.app.profile.dto.UpdatePasswordRequest;
import com.taskOrchestrator.app.profile.dto.UpdateProfileRequest;
import com.taskOrchestrator.app.profile.dto.UserProfileResponse;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.mock.web.MockMultipartFile;
import java.util.UUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserProfileController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({GlobalExceptionHandler.class, UserProfileControllerTest.TestConfig.class})
class UserProfileControllerTest extends AuthenticatedControllerTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        public JwtUtil jwtUtil() {
            return mock(JwtUtil.class);
        }

        @Bean
        public JwtAuthFilter jwtAuthFilter() {
            return mock(JwtAuthFilter.class);
        }
    }

    @MockitoBean
    private UserProfileService userProfileService;

    private static final UUID USER_ID =
            UUID.fromString("11111111-1111-1111-1111-111111111111");

    private UserProfileResponse profileResponse() {
        return new UserProfileResponse(
                USER_ID,
                "testuser",
                "test@example.com",
                "USER",
                "Test User",
                null
        );
    }

    @Nested
    class GetProfile {

        @Test
        void shouldReturnAuthenticatedUserProfile() throws Exception {

            UserProfileResponse response = profileResponse();

            given(userProfileService.getProfile())
                    .willReturn(response);

            mockMvc.perform(
                            get("/profile")
                                    .with(authenticatedUser())
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id")
                            .value(USER_ID.toString()))
                    .andExpect(jsonPath("$.username")
                            .value("testuser"))
                    .andExpect(jsonPath("$.email")
                            .value("test@example.com"))
                    .andExpect(jsonPath("$.role")
                            .value("USER"))
                    .andExpect(jsonPath("$.fullName")
                            .value("Test User"));

            verify(userProfileService)
                    .getProfile();
        }
    }

    @Nested
    class UpdateProfile {
        @Test
        void shouldUpdateUserProfile() throws Exception {
            UpdateProfileRequest request =
                    new UpdateProfileRequest(
                            "updated@example.com",
                            "Updated User"
                    );

            UserProfileResponse response =
                    new UserProfileResponse(
                            USER_ID,
                            "testuser",
                            "updated@example.com",
                            "USER",
                            "Updated User",
                            null
                    );

            given(userProfileService.updateProfile(any())).willReturn(response);
            mockMvc.perform(
                    put("/profile").with(authenticatedUser())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email")
                            .value("updated@example.com"))
                    .andExpect(jsonPath("$.fullName")
                            .value("Updated User"));

            verify(userProfileService).updateProfile(request);
        }
    }

    @Nested
    class UpdatePassword {
        @Test
        void shouldUpdatePassword() throws Exception {
            UpdatePasswordRequest request = new UpdatePasswordRequest(
                    "OldPassword123!",
                    "NewPassword123!"
            );

            UserProfileResponse response = profileResponse();
            given(userProfileService.updatePassword(any())).willReturn(response);

            mockMvc.perform(
                    put("/profile/password").with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                    ).andExpect(status().isOk());

            verify(userProfileService).updatePassword(request);
        }
    }

    @Nested
    class UploadAvatar {
        @Test
        void shouldUploadAvatar() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "avatar.png",
                    "image/png",
                    "image-content".getBytes()
            );

            UserProfileResponse response = new UserProfileResponse(
                    USER_ID,
                    "testuser",
                    "test@example.com",
                    "USER",
                    "Test User",
                    "/uploads/avatar.png"
            );

            given(userProfileService.uploadAvatar(any())).willReturn(response);

            mockMvc.perform(multipart("/profile/avatar").file(file)
                            .with(authenticatedUser()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.profilePictureUrl")
                            .value("/uploads/avatar.png"));

            verify(userProfileService).uploadAvatar(any());
        }
    }
}