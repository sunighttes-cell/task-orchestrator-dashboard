package com.taskOrchestrator.app.job.service;

import com.taskOrchestrator.app.job.events.JobStatusChangedEvent;
import com.taskOrchestrator.app.job.model.Execution;
import com.taskOrchestrator.app.job.model.Job;
import com.taskOrchestrator.app.job.model.JobStatus;
import com.taskOrchestrator.app.job.repository.ExecutionRepository;
import com.taskOrchestrator.app.job.repository.JobRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**Simulate orchestration: processes jobs and orchestrates execution.separates orchestration from
 * REST API logic, controllers orchestrate requests, services orchestrate business logic */

@Service
@Slf4j
public class JobProcessorService {

    private static final Duration STUCK_JOB_THRESHOLD = Duration.ofSeconds(60);
    private final JobRepository jobRepository;
    private final ExecutionRepository executionRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final JobProcessorService self;

    public JobProcessorService(
            JobRepository jobRepository,
            ExecutionRepository executionRepository,
            ApplicationEventPublisher applicationEventPublisher,
            @Lazy @Autowired JobProcessorService self
    ) {
        this.jobRepository = jobRepository;
        this.executionRepository = executionRepository;
        this.applicationEventPublisher = applicationEventPublisher;
        this.self = self;
    }

    /***--------------- JOB EXECUTION--------------***/
    @Transactional
    public void processJob(Long jobId) {
        Job job = jobRepository.findById(jobId).orElseThrow();
        if (job.getStatus() != JobStatus.RUNNING) {
            log.warn("Skipping job {} — current status is {}", jobId, job.getStatus());
            return;
        }

        LocalDateTime startedAt = job.getStartedAt();
        try {
            log.info("Processing job id={}", jobId);
            // Simulate work
            Thread.sleep(2000);
            boolean success = Math.random() > 0.3;
            if (success) {
                completeJob(job);
            } else {
                failJob(job, "Random failure");
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            failJob(job, "Job execution interrupted");

        } catch (Exception exception) {
            log.error("Unexpected error processing job {}", jobId, exception);
            failJob(job, exception.getMessage());
        }

        LocalDateTime completedAt = LocalDateTime.now();
        job.setCompletedAt(completedAt);

        Job savedJob = jobRepository.save(job);
        recordExecution(savedJob, startedAt, completedAt);
        publishJobStatusChanged(savedJob);

        log.info("Finished job id={} status={}", savedJob.getId(), savedJob.getStatus());
    }

    /***---------------JOB STATUS TRANSITIONS--------------***/
    private void completeJob(Job job) {
        job.setStatus(JobStatus.COMPLETED);
        job.setFailureReason(null);
        log.info("Job {} completed successfully", job.getId());
    }

    private void failJob(Job job, String failureReason) {
        job.setStatus(JobStatus.FAILED);
        job.setFailureReason(failureReason);
        log.warn("Job {} failed: {}", job.getId(), failureReason);
    }

    /***---------------EXECUTION HISTORY--------------***/
    private void recordExecution(
            Job job,
            LocalDateTime startedAt,
            LocalDateTime completedAt
    ) {
        long durationMs = Duration.between(startedAt, completedAt).toMillis();
        Execution execution =
                Execution.builder()
                        .job(job)
                        .status(job.getStatus())
                        .durationMs(durationMs)
                        .build();
        executionRepository.save(execution);
    }

    /***---------------STUCK JOB RECOVERY--------------***/
    @Scheduled(fixedDelay = 20000)
    @Transactional
    public void recoverStuckJobsPeriodically() {
        LocalDateTime cutoff = LocalDateTime.now().minus(STUCK_JOB_THRESHOLD);
        List<Job> runningJobs = jobRepository.findByStatus(JobStatus.RUNNING);
        List<Job> stuckJobs = new ArrayList<>();

        for (Job job : runningJobs) {
            LocalDateTime startedAt = job.getStartedAt();
            if (startedAt == null || startedAt.isBefore(cutoff)) {
                job.setStatus(JobStatus.QUEUED);
                job.setStartedAt(null);
                stuckJobs.add(job);
            }
        }

        if (stuckJobs.isEmpty()) { return; }
        List<Job> savedJobs = jobRepository.saveAll(stuckJobs);

        for (Job job : savedJobs) {
            log.warn("Recovered stuck job {} back to QUEUED", job.getId());
            publishJobStatusChanged(job);
        }

        log.warn("{} stuck job(s) recovered to QUEUED", stuckJobs.size());
    }

    /***---------------ASYNC EXECUTION--------------***/
    @Async
    public void processJobAsync(Long jobId) {
        self.processJob(jobId);
    }

    /***---------------CLAIM QUEUED JOBS--------------***/
    @Transactional
    public List<Job> claimNextJobs(int limit) {
        List<Job> jobs = jobRepository.findNextJobsForUpdate(
                        JobStatus.QUEUED,
                        PageRequest.of(0, limit));

        if (jobs.isEmpty()) { return jobs; }
        LocalDateTime now = LocalDateTime.now();

        for (Job job : jobs) {
            job.setStatus(JobStatus.RUNNING);
            job.setStartedAt(now);
        }

        List<Job> savedJobs = jobRepository.saveAll(jobs);
        for (Job job : savedJobs) {
            publishJobStatusChanged(job);
        }

        return savedJobs;
    }


    /***--------------- POLLING--------------***/
    @Scheduled(fixedDelay = 5000)
    public void processQueuedJobs() {
        List<Job> jobs = self.claimNextJobs(5);
        for (Job job : jobs) {
            // Route through Spring proxy so @Async applies.
            self.processJobAsync(job.getId());
        }
    }

    /***--------------- SSE EVENT PUBLISHING--------------***/
    private void publishJobStatusChanged(Job job) {
        applicationEventPublisher.publishEvent(
                new JobStatusChangedEvent(job)
        );
    }
}