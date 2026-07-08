package com.taskOrchestrator.app.auth;
import com.taskOrchestrator.app.auth.domain.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldReturnLoginResponse() throws Exception {
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"newUser\", " +
                        " \"password\":\"newUser1234!\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string(TestDataConstants.TEST_ACCESS_TOKEN))
                .andExpect(content().string(TestDataConstants.TEST_REFRESH_TOKEN));
    }

    @Test
    void shouldNotReturnLoginResponseWithInvalidCredentials() throws Exception {
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"invalidUser\", " +
                        " \"password\":\"invalidPassword\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldNotReturnLoginResponseWithInvalidEmail() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"invalidUser\", " +
                                " \"password\":\"newUser1234!\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldNotReturnLoginResponseWithInvalidPassword() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"newUser\", " +
                                " \"password\":\"invalidPassword\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnRefreshTokenResponse() throws Exception {
        mockMvc.perform(post("/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"exampleRefreshToken\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string(TestDataConstants.TEST_REFRESH_TOKEN));
    }

    @Test
    void shouldNotReturnRefreshTokenResponseWithInvalidToken() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"invalidRefreshToken\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnRegisterResponse() throws Exception {
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"newUser\", " +
                        "{\"email\":\"newUser@example.com\", " +
                        "{\"fullName\":\"New User\", " +
                        " \"password\":\"newUser1234!\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string(TestDataConstants.TEST_ACCESS_TOKEN))
                .andExpect(content().string(TestDataConstants.TEST_REFRESH_TOKEN));
    }

    @Test
    void shouldNotReturnRegisterResponseWithDuplicateUsername() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"newUser\", " +
                                "{\"email\":\"newUser@example.com\", " +
                                "{\"fullName\":\"New User\", " +
                                " \"password\":\"newUser1234!\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldNotReturnRegisterResponseWithDuplicateEmail() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"newUser2\", " +
                                "{\"email\":\"newUser@example.com\", " +
                                "{\"fullName\":\"New User\", " +
                                " \"password\":\"newUser1234!\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldNotReturnRegisterResponseWithInvalidEmail() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"newUser\", " +
                                "{\"email\":\"invalidEmail\", " +
                                "{\"fullName\":\"New User\", " +
                                " \"password\":\"newUser1234!\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldNotReturnRegisterResponseWithInvalidPassword() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"newUser\", " +
                                "{\"email\":\"newUser@example.com\", " +
                                "{\"fullName\":\"New User\", " +
                                " \"password\":\"invalidPassword\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldNotReturnRegisterResponseWithInvalidFullName() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"newUser\", " +
                                "{\"email\":\"newUser@example.com\", " +
                                "{\"fullName\":\"\", " +
                                " \"password\":\"newUser1234!\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldNotReturnRegisterResponseWithInvalidUsername() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"\", " +
                                "{\"email\":\"newUser@example.com\", " +
                                "{\"fullName\":\"New User\", " +
                                " \"password\":\"newUser1234!\"}"))
                .andExpect(status().isBadRequest());
    }
}