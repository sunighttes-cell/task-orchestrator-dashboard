package com.taskOrchestrator.app.job;

import com.taskOrchestrator.app.auth.domain.User;
import com.taskOrchestrator.app.auth.domain.UserRepository;
import com.taskOrchestrator.app.job.model.Job;
import com.taskOrchestrator.app.job.model.JobStatus;
import com.taskOrchestrator.app.job.dto.StatusSummary;
import com.taskOrchestrator.app.job.repository.JobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@ActiveProfiles("test")
class JobRepositoryTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private UserRepository userRepository;
    private User user;
    private User secondUser;

    @BeforeEach
    void setUp() {
        user = userRepository.save(
                User.builder()
                        .username("testuser")
                        .email("test@example.com")
                        .fullName("Test User")
                        .password("encoded-password")
                        .role(User.Role.USER)
                        .build()
        );
        secondUser = userRepository.save(
                User.builder()
                        .username("seconduser")
                        .email("second@example.com")
                        .fullName("Second User")
                        .password("encoded-password")
                        .role(User.Role.USER)
                        .build()
        );
    }

    private Job createJob(String name, JobStatus status, User user, boolean deleted) {
        return Job.builder()
                .name(name)
                .status(status)
                .queuedAt(LocalDateTime.now())
                .retryCount(0)
                .deleted(deleted)
                .user(user)
                .build();
    }

    @Nested
    class FindByStatus {
        @Test
        void shouldReturnJobsByStatus() {
            jobRepository.save(createJob(
                    "Queued Job",
                    JobStatus.QUEUED,
                    user,
                    false)
            );

            jobRepository.save(createJob(
                    "Completed Job",
                    JobStatus.COMPLETED,
                    user,
                    false)
            );

            List<Job> result = jobRepository.findByStatus(JobStatus.QUEUED);
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("Queued Job");
        }
    }

    @Nested
    class GetStatusSummaryByUser {
        @Test
        void shouldReturnStatusSummaryForUser() {
            jobRepository.save(createJob(
                    "Queued Job",
                    JobStatus.QUEUED,
                    user,
                    false)
            );

            jobRepository.save(createJob(
                    "Running Job",
                    JobStatus.RUNNING,
                    user,
                    false)
            );

            jobRepository.save(createJob(
                    "Second Queued Job",
                    JobStatus.QUEUED,
                    user,
                    false)
            );

            jobRepository.save(createJob(
                    "Other User Job",
                    JobStatus.COMPLETED,
                    secondUser,
                    false)
            );

            List<StatusSummary> result = jobRepository.getStatusSummaryByUser("testuser");
            assertThat(result).hasSize(2);
            assertThat(result).anySatisfy(summary -> {
                assertThat(summary.getStatus()).isEqualTo(JobStatus.QUEUED);
                assertThat(summary.getCount()).isEqualTo(2);
            });

            assertThat(result).anySatisfy(summary -> {
                assertThat(summary.getStatus()).isEqualTo(JobStatus.RUNNING);
                assertThat(summary.getCount()).isEqualTo(1);
            });
        }

        @Test
        void shouldExcludeDeletedJobs() {
            jobRepository.save(createJob(
                    "Active Job",
                    JobStatus.QUEUED,
                    user,
                    false)
            );

            jobRepository.save(createJob(
                    "Deleted Job",
                    JobStatus.QUEUED,
                    user,
                    true)
            );

            List<StatusSummary> result = jobRepository.getStatusSummaryByUser("testuser");
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getCount()).isEqualTo(1);
        }

        @Test
        void shouldNotReturnOtherUsersJobs() {
            jobRepository.save(createJob(
                    "User Job",
                    JobStatus.QUEUED,
                    user,
                    false)
            );

            jobRepository.save(createJob(
                    "Other User Job",
                    JobStatus.QUEUED,
                    secondUser,
                    false)
            );

            List<StatusSummary> result = jobRepository.getStatusSummaryByUser("testuser");
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getCount()).isEqualTo(1);
        }
    }

    @Nested
    class GetStatusSummary {
        @Test
        void shouldReturnGlobalStatusSummary() {
            jobRepository.save(createJob(
                    "Queued Job",
                    JobStatus.QUEUED,
                    user,
                    false)
            );

            jobRepository.save(createJob(
                    "Running Job",
                    JobStatus.RUNNING,
                    user,
                    false)
            );

            jobRepository.save(createJob(
                    "Deleted Job",
                    JobStatus.FAILED,
                    user,
                    true)
            );

            List<StatusSummary> result = jobRepository.getStatusSummary();
            assertThat(result).hasSize(2);
            assertThat(result).noneMatch(summary ->
                    summary.getStatus().equals(JobStatus.FAILED)
            );
        }
    }

    @Nested
    class CountActive {
        @Test
        void shouldCountOnlyActiveJobs() {
            jobRepository.save(createJob(
                    "Active Job",
                    JobStatus.QUEUED,
                    user,
                    false)
            );

            jobRepository.save(createJob(
                    "Deleted Job",
                    JobStatus.COMPLETED,
                    user,
                    true)
            );

            long result = jobRepository.countActive();
            assertThat(result).isEqualTo(1);
        }
    }

    @Nested
    class CountActiveByUser {
        @Test
        void shouldCountActiveJobsForUser() {
            jobRepository.save(createJob(
                    "User Active Job",
                    JobStatus.QUEUED,
                    user,
                    false)
            );

            jobRepository.save(createJob(
                    "User Deleted Job",
                    JobStatus.COMPLETED,
                    user,
                    true)
            );

            jobRepository.save(createJob(
                    "Other User Job",
                    JobStatus.QUEUED,
                    secondUser,
                    false)
            );

            long result = jobRepository.countActiveByUser("testuser");
            assertThat(result).isEqualTo(1);
        }
    }

    @Nested
    class CountActiveByStatus {
        @Test
        void shouldCountActiveJobsByStatus() {
            jobRepository.save(createJob(
                    "Queued Job 1",
                    JobStatus.QUEUED,
                    user,
                    false)
            );

            jobRepository.save(createJob(
                    "Queued Job 2",
                    JobStatus.QUEUED,
                    user,
                    false)
            );

            jobRepository.save(createJob(
                    "Deleted Queued Job",
                    JobStatus.QUEUED,
                    user,
                    true)
            );

            long result = jobRepository.countActiveByStatus(JobStatus.QUEUED);
            assertThat(result).isEqualTo(2);
        }
    }

    @Nested
    class CountActiveByUserAndStatus {
        @Test
        void shouldCountActiveJobsForUserAndStatus() {
            jobRepository.save(createJob(
                    "User Queued Job",
                    JobStatus.QUEUED,
                    user,
                    false)
            );

            jobRepository.save(createJob(
                    "User Completed Job",
                    JobStatus.COMPLETED,
                    user,
                    false)
            );

            jobRepository.save(createJob(
                    "Other User Queued Job",
                    JobStatus.QUEUED,
                    secondUser,
                    false)
            );

            long result = jobRepository.countActiveByUserAndStatus(
                    JobStatus.QUEUED, "testuser");
            assertThat(result).isEqualTo(1);
        }
    }

    @Nested
    class FindNextJobsForUpdate {
        @Test
        void shouldReturnQueuedJobsInCreatedOrder() {
            LocalDateTime now = LocalDateTime.now();
            Job oldest = createJob(
                    "Oldest Job",
                    JobStatus.QUEUED,
                    user,
                    false
            );

            oldest.setCreatedAt(now.minusMinutes(10));
            Job newest = createJob(
                    "Newest Job",
                    JobStatus.QUEUED,
                    user,
                    false
            );

            newest.setCreatedAt(now.minusMinutes(1));
            jobRepository.save(oldest);
            jobRepository.save(newest);
            List<Job> result = jobRepository.findNextJobsForUpdate(
                    JobStatus.QUEUED,
                    PageRequest.of(0, 1)
            );

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("Oldest Job");
        }
    }

    @Nested
    class FindNextJobsForUpdateByUser {
        @Test
        void shouldReturnJobsForSpecificUser() {
            jobRepository.save(createJob(
                    "User Queued Job",
                    JobStatus.QUEUED,
                    user,
                    false)
            );

            jobRepository.save(createJob(
                    "Other User Queued Job",
                    JobStatus.QUEUED,
                    secondUser,
                    false)
            );

            List<Job> result = jobRepository.findNextJobsForUpdateByUser(
                    JobStatus.QUEUED,
                    PageRequest.of(0, 10),
                    "testuser"
            );

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getUser().getUsername()).isEqualTo("testuser");
        }
    }

    @Nested
    class FindTop5ByStatus {
        @Test
        void shouldReturnAtMostFiveJobs() {
            for (int i = 1; i <= 6; i++) {
                jobRepository.save(createJob(
                        "Queued Job " + i,
                        JobStatus.QUEUED,
                        user,
                        false)
                );
            }

            List<Job> result = jobRepository.findTop5ByStatusOrderByCreatedAtAsc(
                    JobStatus.QUEUED);
            assertThat(result).hasSize(5);
        }
    }
}