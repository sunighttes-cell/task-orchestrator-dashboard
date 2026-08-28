package com.taskOrchestrator.app.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskOrchestrator.app.auth.domain.RefreshTokenRepository;
import com.taskOrchestrator.app.auth.domain.User;
import com.taskOrchestrator.app.auth.domain.UserRepository;
import com.taskOrchestrator.app.common.support.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end auth tests hitting the real HTTP endpoints, real database
 * (PostgreSQL via testcontainers), and real BCrypt password encoder.
 *
 * These tests exist to prove the security invariants that unit tests
 * cannot: passwords are never persisted in plaintext, credentials are
 * checked against a real BCrypt hash, and 401 is returned on mismatch.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
class AuthIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:15");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void cleanDatabase() {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Nested
    @DisplayName("Registration password hashing")
    class Registration {

        @Test
        @DisplayName("Given plaintext password when registering then database contains BCrypt hash")
        void shouldStoreBcryptHashOnRegistration() throws Exception {

            mockMvc.perform(
                            post("/auth/register")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(TestData.VALID_REGISTER_JSON)
                    )
                    .andExpect(status().isOk());

            Optional<User> saved =
                    userRepository.findByUsername(TestData.USERNAME);

            assertThat(saved).isPresent();

            String stored = saved.get().getPassword();

            // BCrypt hashes start with $2a$, $2b$, or $2y$
            assertThat(stored)
                    .as("password must be persisted as a BCrypt hash")
                    .matches("^\\$2[aby]\\$\\d{2}\\$.{53}$");

            // The stored hash must verify against the original plaintext.
            assertThat(passwordEncoder.matches(
                    TestData.PASSWORD,
                    stored
            )).isTrue();
        }

        @Test
        @DisplayName("Stored password is different from submitted plaintext")
        void shouldNeverStorePlaintextPassword() throws Exception {

            mockMvc.perform(
                            post("/auth/register")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(TestData.VALID_REGISTER_JSON)
                    )
                    .andExpect(status().isOk());

            User saved =
                    userRepository.findByUsername(TestData.USERNAME)
                            .orElseThrow();

            assertThat(saved.getPassword())
                    .as("stored password must not equal submitted plaintext")
                    .isNotEqualTo(TestData.PASSWORD);

            assertThat(saved.getPassword())
                    .as("stored password must not contain the plaintext")
                    .doesNotContain(TestData.PASSWORD);
        }
    }

    @Nested
    @DisplayName("Login credential verification")
    class Login {

        @BeforeEach
        void registerUser() throws Exception {
            mockMvc.perform(
                            post("/auth/register")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(TestData.VALID_REGISTER_JSON)
                    )
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Correct password results in successful login")
        void shouldLoginWithCorrectPassword() throws Exception {

            mockMvc.perform(
                            post("/auth/login")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(TestData.VALID_LOGIN_JSON)
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").exists())
                    .andExpect(jsonPath("$.refreshToken").exists());
        }

        @Test
        @DisplayName("Incorrect password returns 401")
        void shouldReturn401OnIncorrectPassword() throws Exception {

            String wrongPasswordLogin = """
                    {
                      "username":"%s",
                      "password":"%s"
                    }
                    """.formatted(TestData.USERNAME, TestData.WRONG_PASSWORD);

            mockMvc.perform(
                            post("/auth/login")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(wrongPasswordLogin)
                    )
                    .andExpect(status().isUnauthorized());
        }
    }
}
