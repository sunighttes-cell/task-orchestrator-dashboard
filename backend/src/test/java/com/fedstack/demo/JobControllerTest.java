package com.fedstack.demo;

import com.fedstack.demo.model.Job;
import com.fedstack.demo.model.JobStatus;
import com.fedstack.demo.repository.JobRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class JobControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JobRepository jobRepository;

    @Test
    void shouldReturnJobs() throws Exception {
        mockMvc.perform(get("/jobs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }
    //test post
    @Test
    void shouldCreateJob() throws Exception {
        mockMvc.perform(post("/jobs")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Test Job\", " +
                        "\"description\":\"Test Description\"," +
                        "\"deleted\":false," +
                        " \"retryCount\":3," +
                        " \"status\":\"QUEUED\"," +
                        " \"queuedAt\":\"2026-05-10T12:00:00\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Job"));
    }

    //test get with filters
    @Test
    void shouldFilterJobs() throws Exception {
        Job job = new Job();
        job.setName("Test Job");
        job.setDescription("Test Description");
        job.setDeleted(false);
        job.setRetryCount(3);
        job.setStatus(JobStatus.QUEUED);
        job.setQueuedAt(LocalDateTime.now());
        jobRepository.save(job);

        mockMvc.perform(get("/jobs?name=Test Job"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Test Job"));
    }

    //test delete
    @Test
    void shouldDeleteCompletedJob() throws Exception {
        Job job = new Job();
        job.setName("Test Job");
        job.setDescription("Test Description");
        job.setDeleted(false);
        job.setRetryCount(3);
        job.setStatus(JobStatus.COMPLETED);
        jobRepository.saveAndFlush(job);

        mockMvc.perform(delete("/jobs/{id}", job.getId()))
                .andExpect(status().isNoContent());
    }
    @Test
    void shouldNotDeleteRunningJob() throws Exception {
        Job job = new Job();
        job.setName("Test Job");
        job.setDescription("Test Description");
        job.setDeleted(false);
        job.setRetryCount(3);
        job.setStatus(JobStatus.RUNNING);
        jobRepository.saveAndFlush(job);

        mockMvc.perform(delete("/jobs/{id}", job.getId()))
                .andExpect(status().isConflict());
    }

    //validation tests
    @Test
    void shouldFailWhenNameIsBlank() throws Exception {
        String json = """
        { "name": "" }
    """;

        mockMvc.perform(post("/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailWhenNameIsNull() throws Exception {
        String json = """
        { "name": null }
    """;

        mockMvc.perform(post("/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailWhenNameIsTooLong() throws Exception {
        String json = "a".repeat(256);
        String longName = "{\"name\":\""+json +"\"}" ;

        mockMvc.perform(post("/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(longName))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn404_whenJobNotFound() throws Exception {
        mockMvc.perform(delete("/jobs/{id}", 999999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("JOB_NOT_FOUND"));
    }

    @Test
    void shouldReturn409_whenInvalidState() throws Exception {
        Job job = new Job();
        job.setName("Test Job");
        job.setDescription("Test Description");
        job.setDeleted(false);
        job.setRetryCount(3);
        job.setStatus(JobStatus.RUNNING);
        jobRepository.saveAndFlush(job);

        mockMvc.perform(delete("/jobs/{id}", job.getId()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INVALID_JOB_STATE"));
    }

    @Test
    void shouldReturnValidationErrors() throws Exception {
        String invalidBody = """
        {
          "name": ""
        }
        """;

        mockMvc.perform(post("/jobs")
                        .contentType("application/json")
                        .content(invalidBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors.name").exists());
    }
}