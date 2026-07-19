package com.taskOrchestrator.app.job;
import com.taskOrchestrator.app.auth.application.CurrentUser;
import com.taskOrchestrator.app.auth.application.CurrentUserProvider;
import com.taskOrchestrator.app.auth.domain.User;
import com.taskOrchestrator.app.auth.domain.UserRepository;
import com.taskOrchestrator.app.job.model.Job;
import com.taskOrchestrator.app.job.model.JobStatus;
import com.taskOrchestrator.app.job.dto.CreateJobRequest;
import com.taskOrchestrator.app.job.dto.JobResponse;
import com.taskOrchestrator.app.job.dto.StatusSummary;
import com.taskOrchestrator.app.common.exception.JobNotFoundException;
import com.taskOrchestrator.app.job.repository.JobRepository;
import com.taskOrchestrator.app.job.service.JobService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private JobService jobService;
    private User user;
    private CurrentUser currentUser;
    private Job job;
    private static final Long JOB_ID = 1L;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .fullName("Test User")
                .password("encoded-password")
                .role(User.Role.USER)
                .build();

        currentUser = new CurrentUser("testuser", User.Role.USER);
        job = Job.builder()
                .id(JOB_ID)
                .name("Test Job")
                .status(JobStatus.QUEUED)
                .queuedAt(LocalDateTime.now())
                .retryCount(0)
                .deleted(false)
                .user(user)
                .build();
    }

    @Nested
    class CreateJob {
        @Test
        void shouldCreateQueuedJobForAuthenticatedUser() {
            CreateJobRequest request =
                    new CreateJobRequest("New Job");
            given(currentUserProvider.getCurrentUser())
                    .willReturn(currentUser);
            given(userRepository.findByUsername("testuser"))
                    .willReturn(Optional.of(user));
            given(jobRepository.save(any(Job.class)))
                    .willAnswer(invocation ->
                            invocation.getArgument(0));
            JobResponse response =
                    jobService.createJob(request);
            ArgumentCaptor<Job> jobCaptor =
                    ArgumentCaptor.forClass(Job.class);
            verify(jobRepository)
                    .save(jobCaptor.capture());

            Job savedJob = jobCaptor.getValue();
            assertThat(savedJob.getName()).isEqualTo("New Job");
            assertThat(savedJob.getStatus()).isEqualTo(JobStatus.QUEUED);
            assertThat(savedJob.getRetryCount()).isZero();
            assertThat(savedJob.getUser()).isSameAs(user);
            assertThat(savedJob.getQueuedAt()).isNotNull();
            assertThat(response).isNotNull();
        }

        @Test
        void shouldThrowWhenAuthenticatedUserDoesNotExist() {
            CreateJobRequest request = new CreateJobRequest("New Job");

            given(currentUserProvider.getCurrentUser())
                    .willReturn(currentUser);

            given(userRepository.findByUsername("testuser"))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() ->
                    jobService.createJob(request)
            )
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("User not found");

            verify(jobRepository, never())
                    .save(any(Job.class));
        }
    }

    @Nested
    class GetJobs {
        @Test
        void shouldReturnJobsForAuthenticatedUser() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Job> jobs = new PageImpl<>(
                        List.of(job),
                        pageable,
                        1
            );

            given(currentUserProvider.getCurrentUser())
                    .willReturn(currentUser);

            given(jobRepository.findAll(
                    any(Specification.class),
                    eq(pageable)
            )).willReturn(jobs);

            Page<JobResponse> result = jobService.getJobs(
                    null,
                    null,
                    null,
                    null,
                    pageable
            );

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getName()).isEqualTo("Test Job");
            verify(jobRepository).findAll(
                    any(Specification.class),
                    eq(pageable)
            );
        }

        @Test
        void shouldApplyStatusFilter() {

            Pageable pageable =
                    PageRequest.of(0, 10);

            Page<Job> jobs =
                    new PageImpl<>(
                            List.of(job),
                            pageable,
                            1
                    );

            given(currentUserProvider.getCurrentUser())
                    .willReturn(currentUser);

            given(jobRepository.findAll(
                    any(Specification.class),
                    eq(pageable)
            ))
                    .willReturn(jobs);

            Page<JobResponse> result =
                    jobService.getJobs(
                            JobStatus.QUEUED,
                            null,
                            null,
                            null,
                            pageable
                    );

            assertThat(result.getContent())
                    .hasSize(1);

            verify(jobRepository)
                    .findAll(
                            any(Specification.class),
                            eq(pageable)
                    );
        }

        @Test
        void shouldApplySearchFilter() {

            Pageable pageable =
                    PageRequest.of(0, 10);

            Page<Job> jobs =
                    new PageImpl<>(
                            List.of(job),
                            pageable,
                            1
                    );

            given(currentUserProvider.getCurrentUser())
                    .willReturn(currentUser);

            given(jobRepository.findAll(
                    any(Specification.class),
                    eq(pageable)
            ))
                    .willReturn(jobs);

            Page<JobResponse> result =
                    jobService.getJobs(
                            null,
                            "  test  ",
                            null,
                            null,
                            pageable
                    );

            assertThat(result.getContent())
                    .hasSize(1);

            verify(jobRepository)
                    .findAll(
                            any(Specification.class),
                            eq(pageable)
                    );
        }

        @Test
        void shouldApplyRetryCountFilter() {

            Pageable pageable =
                    PageRequest.of(0, 10);

            Page<Job> jobs =
                    new PageImpl<>(
                            List.of(job),
                            pageable,
                            1
                    );

            given(currentUserProvider.getCurrentUser())
                    .willReturn(currentUser);

            given(jobRepository.findAll(
                    any(Specification.class),
                    eq(pageable)
            ))
                    .willReturn(jobs);

            Page<JobResponse> result =
                    jobService.getJobs(
                            null,
                            null,
                            null,
                            2,
                            pageable
                    );

            assertThat(result.getContent())
                    .hasSize(1);

            verify(jobRepository)
                    .findAll(
                            any(Specification.class),
                            eq(pageable)
                    );
        }

        @Test
        void shouldApplyCreatedAfterFilter() {

            Pageable pageable =
                    PageRequest.of(0, 10);

            LocalDateTime createdAfter =
                    LocalDateTime.now().minusDays(1);

            Page<Job> jobs =
                    new PageImpl<>(
                            List.of(job),
                            pageable,
                            1
                    );

            given(currentUserProvider.getCurrentUser())
                    .willReturn(currentUser);

            given(jobRepository.findAll(
                    any(Specification.class),
                    eq(pageable)
            ))
                    .willReturn(jobs);

            Page<JobResponse> result =
                    jobService.getJobs(
                            null,
                            null,
                            createdAfter,
                            null,
                            pageable
                    );

            assertThat(result.getContent())
                    .hasSize(1);

            verify(jobRepository)
                    .findAll(
                            any(Specification.class),
                            eq(pageable)
                    );
        }
    }

    @Nested
    class RetryJob {

        @Test
        void shouldRetryFailedJob() {

            job.setStatus(JobStatus.FAILED);
            job.setRetryCount(1);
            job.setStartedAt(LocalDateTime.now().minusMinutes(5));
            job.setCompletedAt(LocalDateTime.now().minusMinutes(1));

            given(currentUserProvider.getCurrentUser())
                    .willReturn(currentUser);

            given(jobRepository.findOne(any(Specification.class)))
                    .willReturn(Optional.of(job));

            given(jobRepository.save(job))
                    .willReturn(job);

            JobResponse response =
                    jobService.retryJob(JOB_ID);

            assertThat(job.getStatus())
                    .isEqualTo(JobStatus.QUEUED);

            assertThat(job.getRetryCount())
                    .isEqualTo(2);

            assertThat(job.getStartedAt())
                    .isNull();

            assertThat(job.getCompletedAt())
                    .isNull();

            assertThat(job.getUpdatedAt())
                    .isNotNull();

            verify(jobRepository)
                    .save(job);

            assertThat(response)
                    .isNotNull();
        }

        @Test
        void shouldThrowWhenJobDoesNotExist() {

            given(currentUserProvider.getCurrentUser())
                    .willReturn(currentUser);

            given(jobRepository.findOne(any(Specification.class)))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() ->
                    jobService.retryJob(JOB_ID)
            )
                    .isInstanceOf(JobNotFoundException.class);

            verify(jobRepository, never())
                    .save(any(Job.class));
        }

        @Test
        void shouldRejectRetryWhenJobIsNotFailed() {

            job.setStatus(JobStatus.COMPLETED);

            given(currentUserProvider.getCurrentUser())
                    .willReturn(currentUser);

            given(jobRepository.findOne(any(Specification.class)))
                    .willReturn(Optional.of(job));

            assertThatThrownBy(() ->
                    jobService.retryJob(JOB_ID)
            )
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage(
                            "Job must be in FAILED status to retry"
                    );

            verify(jobRepository, never())
                    .save(any(Job.class));
        }
    }

    @Nested
    class GetStatusSummary {

        @Test
        void shouldReturnStatusSummaryForAuthenticatedUser() {
            List<StatusSummary> summary = List.of();

            given(currentUserProvider.getCurrentUser())
                    .willReturn(currentUser);

            given(jobRepository.getStatusSummaryByUser("testuser"))
                    .willReturn(summary);

            List<StatusSummary> result =
                    jobService.getStatusSummary();

            assertThat(result)
                    .isSameAs(summary);

            verify(jobRepository)
                    .getStatusSummaryByUser("testuser");
        }
    }

    @Nested
    class DeleteJob {
        @Test
        void shouldSoftDeleteJob() {
            Pageable pageable = PageRequest.of(0, 10);
            job.setStatus(JobStatus.COMPLETED);
            given(currentUserProvider.getCurrentUser())
                    .willReturn(currentUser);
            given(jobRepository.findOne(any(Specification.class)))
                    .willReturn(Optional.of(job));
            jobService.deleteJob(JOB_ID, pageable);
            assertThat(job.getDeleted()).isTrue();
            verify(jobRepository).save(job);
        }

        @Test
        void shouldThrowWhenJobDoesNotExist() {
            Pageable pageable = PageRequest.of(0, 10);
            given(currentUserProvider.getCurrentUser()).willReturn(currentUser);
            given(jobRepository.findOne(any(Specification.class)))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> jobService.deleteJob(JOB_ID, pageable))
                    .isInstanceOf(JobNotFoundException.class);

            verify(jobRepository, never()).save(any(Job.class));
        }

        @Test
        void shouldRejectDeletingRunningJob() {
            Pageable pageable = PageRequest.of(0, 10);
            job.setStatus(JobStatus.RUNNING);
            given(currentUserProvider.getCurrentUser())
                    .willReturn(currentUser);
            given(jobRepository.findOne(any(Specification.class)))
                    .willReturn(Optional.of(job));
            assertThatThrownBy(() ->
                    jobService.deleteJob(JOB_ID, pageable)
            )
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Cannot delete a running job");
            verify(jobRepository, never()).save(any(Job.class));
        }
    }
}