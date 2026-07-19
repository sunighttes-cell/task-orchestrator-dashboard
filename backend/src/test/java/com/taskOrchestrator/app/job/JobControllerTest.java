package com.taskOrchestrator.app.job;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskOrchestrator.app.auth.domain.User;
import com.taskOrchestrator.app.auth.infrastructure.jwt.JwtUtil;
import com.taskOrchestrator.app.auth.web.AuthController;
import com.taskOrchestrator.app.common.controller.AuthenticatedControllerTest;
import com.taskOrchestrator.app.common.exception.GlobalExceptionHandler;
import com.taskOrchestrator.app.config.SecurityConfig;
import com.taskOrchestrator.app.job.model.Job;
import com.taskOrchestrator.app.job.service.DashboardService;
import com.taskOrchestrator.app.job.service.JobService;
import com.taskOrchestrator.app.job.controller.JobController;
import com.taskOrchestrator.app.job.model.JobStatus;
import com.taskOrchestrator.app.job.dto.CreateJobRequest;
import com.taskOrchestrator.app.job.dto.JobResponse;
import com.taskOrchestrator.app.job.dto.StatusSummary;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

@WebMvcTest(JobController.class)
@Import(GlobalExceptionHandler.class)
class JobControllerTest extends AuthenticatedControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JobService jobService;

    @MockitoBean
    private DashboardService dashboardService;

//    @MockitoBean
//    protected JwtUtil jwtUtil;

    private static final Long JOB_ID = 1L;

    private JobResponse jobResponse() {
        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .fullName("Test User")
                .password("encoded-password")
                .role(User.Role.USER)
                .build();

        Job job = Job.builder()
                .id(JOB_ID)
                .name("Test Job")
                .status(JobStatus.QUEUED)
                .queuedAt(LocalDateTime.now())
                .retryCount(0)
                .deleted(false)
                .failureReason(null)
                .user(user)
                .build();

        return JobResponse.fromJob(job);
    }

    @Nested
    class CreateJob {
        @Test
        void shouldCreateJob() throws Exception {
            CreateJobRequest request = new CreateJobRequest("New Job");
            JobResponse response = jobResponse();
            given(jobService.createJob(any())).willReturn(response);
            mockMvc.perform(
                    post("/jobs")
                            .with(authenticatedUser())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id")
                            .value(JOB_ID))
                    .andExpect(jsonPath("$.name")
                            .value("Test Job"))
                    .andExpect(jsonPath("$.status")
                            .value("QUEUED"));

            verify(jobService).createJob(request);
        }
    }

    @Nested
    class GetJobs {
        @Test
        void shouldReturnJobs() throws Exception {
            JobResponse response = jobResponse();
            given(jobService.getJobs(any(), any(), any(), any(), any()))
                    .willReturn(
                        new PageImpl<>(List.of(response),
                                PageRequest.of(0, 10), 1));

            mockMvc.perform(get("/jobs")
                            .with(authenticatedUser()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content")
                            .isArray())
                    .andExpect(jsonPath("$.content[0].id")
                            .value(JOB_ID))
                    .andExpect(jsonPath("$.content[0].name")
                            .value("Test Job"));

            verify(jobService)
                    .getJobs(eq(null), eq(null), eq(null), eq(null), any());
        }

        @Test
        void shouldFilterJobsByStatus() throws Exception {
            given(jobService.getJobs(any(), any(), any(), any(), any()))
                    .willReturn(new PageImpl<>(List.of(jobResponse())));

            mockMvc.perform(get("/jobs").param(
                    "status", "FAILED").with(authenticatedUser()))
                    .andExpect(status().isOk());

            verify(jobService).getJobs(eq(JobStatus.FAILED),
                            eq(null), eq(null), eq(null), any());
        }
    }

    @Nested
    class RetryJob {
        @Test
        void shouldRetryJob() throws Exception {
            given(jobService.retryJob(JOB_ID)).willReturn(jobResponse());

            mockMvc.perform(post("/jobs/{id}/retry", JOB_ID).with(authenticatedUser()))
                    .andExpect(status().isOk());
            verify(jobService).retryJob(JOB_ID);
        }
    }

    @Nested
    class GetStatusSummary {
        @Test
        void shouldReturnStatusSummary() throws Exception {
            List<StatusSummary> summary = List.of();
            given(jobService.getStatusSummary()).willReturn(summary);

            mockMvc.perform(get("/jobs/status-summary").with(authenticatedUser()))
                    .andExpect(status().isOk());
            verify(jobService).getStatusSummary();
        }
    }

    @Nested
    class DeleteJob {
        @Test
        void shouldDeleteJob() throws Exception {
            mockMvc.perform(delete("/jobs/{id}", JOB_ID)
                            .with(authenticatedUser()))
                    .andExpect(status().isNoContent());
            verify(jobService).deleteJob(eq(JOB_ID), any());
        }
    }
}
