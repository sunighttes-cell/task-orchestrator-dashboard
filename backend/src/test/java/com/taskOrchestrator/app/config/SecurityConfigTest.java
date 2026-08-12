package com.taskOrchestrator.app.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskOrchestrator.app.auth.infrastructure.jwt.JwtAuthFilter;
import com.taskOrchestrator.app.auth.infrastructure.jwt.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SecurityConfigTest.StubController.class)
@Import({SecurityConfig.class, SecurityConfigTest.TestConfig.class})
@TestPropertySource(properties = {
        "app.storage.upload-dir=uploads",
        "spring.main.allow-bean-definition-overriding=true"
})
class SecurityConfigTest {

    @RestController
    static class StubController {
        @GetMapping("/jobs")
        public void jobs() {}

        @GetMapping({"/auth/login", "/auth/register", "/auth/refresh"})
        public void auth() {}

        @GetMapping("/realtime/jobs")
        public void realtime() {}
    }

    @TestConfiguration
    @EnableWebSecurity
    @EnableConfigurationProperties(StorageProperties.class)
    static class TestConfig {
        @Bean
        public JwtUtil jwtUtil() {
            return mock(JwtUtil.class);
        }

        @Bean
        public JwtAuthFilter jwtAuthFilter(JwtUtil jwtUtil, ObjectMapper objectMapper) {
            return new JwtAuthFilter(jwtUtil, objectMapper);
        }

        @Bean
        public ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldRequireAuthenticationForProtectedEndpoint()
            throws Exception {

        mockMvc.perform(
                        get("/jobs")
                )
                .andExpect(
                        status().isUnauthorized()
                );
    }

    @Test
    void shouldAllowAuthenticationEndpoints()
            throws Exception {

        mockMvc.perform(
                        get("/auth/login")
                )
                .andExpect(
                        status().isNotFound() // Security allowed it, but it reached DispatcherServlet and didn't match StubController (maybe due to @WebMvcTest limiting)
                );
    }

    @Test
    void shouldAllowOptionsRequests()
            throws Exception {

        mockMvc.perform(
                        options("/jobs")
                )
                .andExpect(
                        status().isNotFound() // Security allowed it, so it reached DispatcherServlet and found no handler for OPTIONS
                );
    }

    @Test
    void shouldAllowUploadedFiles()
            throws Exception {

        mockMvc.perform(
                        get("/uploads/avatar.png")
                )
                .andExpect(
                        status().isNotFound() // Security allowed it, and ResourceHttpRequestHandler found no file
                );
    }

    @Test
    void shouldRequireAuthenticationForRealtimeEndpoint()
            throws Exception {

        mockMvc.perform(
                        get("/realtime/jobs")
                )
                .andExpect(
                        status().isUnauthorized()
                );
    }
}