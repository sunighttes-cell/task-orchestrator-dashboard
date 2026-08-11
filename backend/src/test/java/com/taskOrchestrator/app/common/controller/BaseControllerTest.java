package com.taskOrchestrator.app.common.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskOrchestrator.app.common.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**Base class for all controller tests.Responsibilities: Shared MockMvc, Shared ObjectMapper,
 * GlobalExceptionHandler, JSON helper methods. Controller tests should focus ONLY on:
 * request mapping, HTTP status, JSON serialization, JSON response, Business logic belongs in Service tests.*/
@Import(GlobalExceptionHandler.class)
public abstract class BaseControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    protected MediaType json = MediaType.APPLICATION_JSON;

    @BeforeEach
    void setUp() {
        // Shared setup for all controller tests. Override in subclasses if needed.
    }

    // Convert an object into JSON.
    protected String toJson(Object object) throws Exception {
        return objectMapper.writeValueAsString(object);
    }

    //Convenience method for pretty-printing JSON while debugging failing tests.
    protected String prettyJson(Object object) throws Exception {
        return objectMapper
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(object);
    }
}