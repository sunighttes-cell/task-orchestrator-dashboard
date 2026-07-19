package com.taskOrchestrator.app.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskOrchestrator.app.auth.application.AuthService;
import com.taskOrchestrator.app.auth.infrastructure.jwt.JwtUtil;
import com.taskOrchestrator.app.auth.web.AuthAccessResponse;
import com.taskOrchestrator.app.auth.web.AuthController;
import com.taskOrchestrator.app.auth.web.RegisterRequest;
import com.taskOrchestrator.app.common.controller.BaseControllerTest;
import com.taskOrchestrator.app.common.exception.DuplicateEmailException;
import com.taskOrchestrator.app.common.exception.DuplicateUsernameException;
import com.taskOrchestrator.app.common.exception.GlobalExceptionHandler;
import com.taskOrchestrator.app.common.support.TestData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(AuthController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest extends BaseControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @Nested
    class Login {

        @Test
        @DisplayName("Should login successfully")
        void shouldLoginSuccessfully() throws Exception {

            AuthAccessResponse response =
                    new AuthAccessResponse(
                            TestData.ACCESS_TOKEN,
                            TestData.REFRESH_TOKEN
                    );

            when(authService.login(
                    TestData.USERNAME,
                    TestData.PASSWORD))
                    .thenReturn(response);

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(TestData.VALID_LOGIN_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken")
                            .value(TestData.ACCESS_TOKEN))
                    .andExpect(jsonPath("$.refreshToken")
                            .value(TestData.REFRESH_TOKEN));
        }

        @Test
        @DisplayName("Should return 401 for invalid credentials")
        void shouldReturnUnauthorized() throws Exception {

            when(authService.login(anyString(), anyString()))
                    .thenThrow(new ResponseStatusException(
                            UNAUTHORIZED,
                            "Invalid username or password"));

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(TestData.INVALID_LOGIN_JSON))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message")
                            .value("Invalid username or password"));
        }
    }

    @Nested
    class Refresh {

        @Test
        @DisplayName("Should refresh access token")
        void shouldRefreshSuccessfully() throws Exception {

            AuthAccessResponse response =
                    new AuthAccessResponse(
                            TestData.ACCESS_TOKEN,
                            TestData.REFRESH_TOKEN);

            when(authService.refresh(TestData.REFRESH_TOKEN))
                    .thenReturn(response);

            mockMvc.perform(post("/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(TestData.VALID_REFRESH_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken")
                            .value(TestData.ACCESS_TOKEN))
                    .andExpect(jsonPath("$.refreshToken")
                            .value(TestData.REFRESH_TOKEN));
        }

        @Test
        @DisplayName("Should reject invalid refresh token")
        void shouldRejectInvalidRefreshToken() throws Exception {

            when(authService.refresh(anyString()))
                    .thenThrow(new ResponseStatusException(
                            UNAUTHORIZED,
                            "Invalid refresh token"));

            mockMvc.perform(post("/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(TestData.INVALID_REFRESH_JSON))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message")
                            .value("Invalid refresh token"));
        }
    }

    @Nested
    class Register {

        @Test
        @DisplayName("Should register successfully")
        void shouldRegisterSuccessfully() throws Exception {

            RegisterRequest request =
                    new RegisterRequest(
                            TestData.USERNAME,
                            TestData.EMAIL,
                            TestData.FULL_NAME,
                            TestData.PASSWORD);

            AuthAccessResponse response =
                    new AuthAccessResponse(
                            TestData.ACCESS_TOKEN,
                            TestData.REFRESH_TOKEN);

            when(authService.register(any(RegisterRequest.class)))
                    .thenReturn(response);

            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken")
                            .value(TestData.ACCESS_TOKEN))
                    .andExpect(jsonPath("$.refreshToken")
                            .value(TestData.REFRESH_TOKEN));
        }

        @Test
        @DisplayName("Should return conflict when username already exists")
        void shouldRejectDuplicateUsername() throws Exception {

            when(authService.register(any(RegisterRequest.class)))
                    .thenThrow(new DuplicateUsernameException(
                            "Username already exists"));

            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(TestData.DUPLICATE_USERNAME_REGISTER_JSON))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("Should return conflict when email already exists")
        void shouldRejectDuplicateEmail() throws Exception {

            when(authService.register(any(RegisterRequest.class)))
                    .thenThrow(new DuplicateEmailException(
                            "Email already exists"));

            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(TestData.DUPLICATE_EMAIL_REGISTER_JSON))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("Should reject invalid email")
        void shouldRejectInvalidEmail() throws Exception {

            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(TestData.INVALID_EMAIL_REGISTER_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject invalid password")
        void shouldRejectInvalidPassword() throws Exception {

            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(TestData.INVALID_PASSWORD_REGISTER_JSON))
                    .andExpect(status().isBadRequest());
        }
    }
}