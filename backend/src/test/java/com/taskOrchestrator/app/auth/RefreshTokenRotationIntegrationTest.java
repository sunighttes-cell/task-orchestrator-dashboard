package com.taskOrchestrator.app.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskOrchestrator.app.auth.domain.RefreshToken;
import com.taskOrchestrator.app.auth.domain.RefreshTokenRepository;
import com.taskOrchestrator.app.auth.domain.UserRepository;
import com.taskOrchestrator.app.common.support.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end proof of the refresh-token rotation and reuse-detection flow.
 *
 * The flow this test locks in:
 *   LOGIN
 *     ↓
 *   Token A returned
 *     ↓
 *   POST /auth/refresh with Token A
 *     ↓
 *   Token B returned
 *     ↓
 *   Token A is revoked (in DB)
 *     ↓
 *   POST /auth/refresh with Token A again
 *     ↓
 *   401
 *     ↓
 *   All the user's refresh sessions are revoked (reuse detection)
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
class RefreshTokenRotationIntegrationTest {

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

    @BeforeEach
    void cleanDatabase() {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName(
            "LOGIN → Token A → refresh(A) → Token B (A revoked) → "
                    + "refresh(A) again → 401, all sessions revoked"
    )
    void shouldRotateAndRevokeOnReuse() throws Exception {

        // ─── LOGIN ───
        register();
        String tokenA = login();

        assertThat(findByRaw(tokenA))
                .as("Token A must be persisted after login")
                .isNotNull();

        assertThat(findByRaw(tokenA).getRevokedAt())
                .as("Token A must be active immediately after login")
                .isNull();

        // ─── POST /auth/refresh with Token A → Token B returned ───
        String tokenB = refresh(tokenA, status().isOk());

        assertThat(tokenB)
                .as("Rotation must return a new refresh token distinct from Token A")
                .isNotEqualTo(tokenA);

        // ─── Token A is revoked (in DB) ───
        RefreshToken tokenAAfterRotation = findByRaw(tokenA);

        assertThat(tokenAAfterRotation.getRevokedAt())
                .as("Token A must be marked revoked after rotation")
                .isNotNull();

        // Token B, meanwhile, should be active and persisted.
        RefreshToken tokenBRecord = findByRaw(tokenB);

        assertThat(tokenBRecord)
                .as("Token B must be persisted")
                .isNotNull();

        assertThat(tokenBRecord.getRevokedAt())
                .as("Token B must be active after rotation")
                .isNull();

        // ─── POST /auth/refresh with Token A again → 401 ───
        refresh(tokenA, status().isUnauthorized());

        // ─── All the user's refresh sessions must now be revoked ───
        List<RefreshToken> allTokens =
                refreshTokenRepository.findAll();

        assertThat(allTokens)
                .as("User must have at least Token A and Token B recorded")
                .hasSizeGreaterThanOrEqualTo(2);

        assertThat(allTokens)
                .as("Every refresh session for the user must be revoked "
                        + "after reuse of Token A is detected")
                .allSatisfy(token ->
                        assertThat(token.getRevokedAt())
                                .as("token id=%s must have revokedAt set",
                                        token.getId())
                                .isNotNull()
                );

        // Belt-and-braces: no active sessions remain.
        assertThat(refreshTokenRepository.findByRevokedAtIsNull())
                .as("There must be zero active refresh tokens after reuse")
                .isEmpty();
    }

    private void register() throws Exception {
        mockMvc.perform(
                        post("/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(TestData.VALID_REGISTER_JSON)
                )
                .andExpect(status().isOk());
    }

    private String login() throws Exception {
        MvcResult result = mockMvc.perform(
                        post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(TestData.VALID_LOGIN_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andReturn();

        JsonNode body = objectMapper.readTree(
                result.getResponse().getContentAsString()
        );

        return body.get("refreshToken").asText();
    }

    private String refresh(
            String refreshToken,
            org.springframework.test.web.servlet.ResultMatcher expectedStatus
    ) throws Exception {

        String requestBody = objectMapper.writeValueAsString(
                java.util.Map.of("refreshToken", refreshToken)
        );

        MvcResult result = mockMvc.perform(
                        post("/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(expectedStatus)
                .andReturn();

        String body = result.getResponse().getContentAsString();

        if (body == null || body.isBlank()) {
            return null;
        }

        JsonNode json = objectMapper.readTree(body);

        if (!json.has("refreshToken")) {
            return null;
        }

        return json.get("refreshToken").asText();
    }

    /** Look up a refresh-token row by the raw JWT the client received. */
    private RefreshToken findByRaw(String rawToken) {
        return refreshTokenRepository
                .findByTokenHash(hash(rawToken))
                .orElse(null);
    }

    private String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(
                    rawToken.getBytes(StandardCharsets.UTF_8)
            );

            StringBuilder hex = new StringBuilder(bytes.length * 2);

            for (byte b : bytes) {
                hex.append(String.format("%02x", b));
            }

            return hex.toString();

        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
}
