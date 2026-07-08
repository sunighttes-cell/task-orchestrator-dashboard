package com.taskOrchestrator.app.profile;
import com.taskOrchestrator.app.auth.domain.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class UserProfileControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    //test get endpoint
    @Test
    void shouldReturnProfile() throws Exception {
        mockMvc.perform(get("/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isNotEmpty());
    }

    //test put endpoint
    @Test
    void shouldUpdateProfile() throws Exception {
        mockMvc.perform(put("/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"New User\", " +
                                " \"email\":\"user@example.com\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fullName").value("Test Update User"));
    }

    //test put endpoint
    @Test
    void shouldNotUpdateProfileWithInvalidData() throws Exception {
        mockMvc.perform(put("/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"New User\"}"))
                .andExpect(status().isBadRequest());
    }

    //test put endpoint
    @Test
    void shouldUNotUpdateProfileWithInvalidEmail() throws Exception {
        mockMvc.perform(put("/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"New User\", " +
                                " \"email\":\"invalidemail\"}"))
                .andExpect(status().isBadRequest());
    }

    //test put endpoint update password
    @Test
    void shouldUpdatePassword() throws Exception {
        mockMvc.perform(put("/profile/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"oldPassword\":\"passwordCurrent1234!\", " +
                                " \"newPassword\":\"exampleNewPw24!\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldNotUpdatePasswordWithInvalidOldPassword() throws Exception {
        mockMvc.perform(put("/profile/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"oldPassword\":\"invalidPassword14$\", " +
                                " \"newPassword\":\"exampleNewPw24!\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldNotUpdatePasswordWithInvalidNewPassword() throws Exception {
        mockMvc.perform(put("/profile/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"oldPassword\":\"passwordCurrent1234!\", " +
                                " \"newPassword\":\"invalidPassword\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldNotUpdatePasswordWithInvalidOldAndNewPassword() throws Exception {
        mockMvc.perform(put("/profile/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"oldPassword\":\"invalidPassword\", " +
                                " \"newPassword\":\"invalidPassword\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldNotUpdatePasswordWithInvalidOldAndNewPasswordLength() throws Exception {
        mockMvc.perform(put("/profile/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"oldPassword\":\"invalid\", " +
                                " \"newPassword\":\"invalid\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldUploadAvatar() throws Exception {
        mockMvc.perform(post("/profile/avatar")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .content("file=test.png"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldNotUploadAvatarWithInvalidFile() throws Exception {
        mockMvc.perform(post("/profile/avatar")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .content("file=test.txt"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldNotUploadAvatarWithInvalidContentType() throws Exception {
        mockMvc.perform(post("/profile/avatar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("file=test.png"))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void shouldNotUploadAvatarWithInvalidFileExtension() throws Exception {
        mockMvc.perform(post("/profile/avatar")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .content("file=test.txt"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldNotUploadAvatarWithInvalidFileSize() throws Exception {
        mockMvc.perform(post("/profile/avatar")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .content("file=test.png"))
                .andExpect(status().isBadRequest());
    }

}