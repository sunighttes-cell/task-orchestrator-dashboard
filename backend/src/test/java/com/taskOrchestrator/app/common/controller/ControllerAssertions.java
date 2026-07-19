package com.taskOrchestrator.app.common.controller;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public final class ControllerAssertions {

    private ControllerAssertions() {
    }

    public static void assertUnauthorized(ResultActions result)
            throws Exception {

        result.andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code")
                        .value("UNAUTHORIZED"));
    }

    public static void assertForbidden(ResultActions result)
            throws Exception {

        result.andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code")
                        .value("FORBIDDEN"));
    }

    public static void assertNotFound(ResultActions result)
            throws Exception {

        result.andExpect(status().isNotFound());
    }

    public static void assertBadRequest(ResultActions result)
            throws Exception {

        result.andExpect(status().isBadRequest());
    }

    public static void assertConflict(ResultActions result)
            throws Exception {

        result.andExpect(status().isConflict());
    }

    public static void assertValidationError(
            ResultActions result,
            String field
    ) throws Exception {

        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code")
                        .value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors." + field)
                        .exists());
    }

    public static void assertTokenExpired(ResultActions result)
            throws Exception {

        result.andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code")
                        .value("TOKEN_EXPIRED"));
    }

    public static void assertInvalidToken(ResultActions result)
            throws Exception {

        result.andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code")
                        .value("INVALID_TOKEN"));
    }
}