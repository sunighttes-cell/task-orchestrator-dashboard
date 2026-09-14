package com.taskOrchestrator.app.job.service;

import com.taskOrchestrator.app.realtime.events.JobStatusChangedEvent;
import com.taskOrchestrator.app.job.model.Execution;
import com.taskOrchestrator.app.job.model.Job;
import com.taskOrchestrator.app.job.model.JobStatus;
import com.taskOrchestrator.app.job.repository.ExecutionRepository;
import com.taskOrchestrator.app.job.repository.JobRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.taskOrchestrator.app.common.logging.LoggingConstants;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
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
            log.warn("Skipping job because it is not RUNNING");
            return;
        }

        LocalDateTime startedAt = job.getStartedAt();

        if (startedAt == null) {
            startedAt = LocalDateTime.now();
            job.setStartedAt(startedAt);
        }

        long executionStart = System.nanoTime();
        Execution execution = Execution.builder()
                .job(job)
                .status(JobStatus.RUNNING)
                .durationMs(0L)
                .build();

        execution = executionRepository.save(execution);
        MDC.put(LoggingConstants.JOB_ID, String.valueOf(job.getId()));
        MDC.put(LoggingConstants.EXECUTION_ID, String.valueOf(execution.getExecutionId()));
        MDC.put(LoggingConstants.USER_ID, job.getUser().getId().toString());
        MDC.put(LoggingConstants.STATUS, JobStatus.RUNNING.name());

        try {
            log.info("Job execution started");
            // Simulate asynchronous work.
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
            log.warn("Job execution interrupted", exception);

        } catch (Exception exception) {
            failJob(job, exception.getMessage());
            log.error("Unexpected error processing job", exception);

        } finally {
            LocalDateTime completedAt = LocalDateTime.now();

            long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - executionStart);

            job.setCompletedAt(completedAt);
            Job savedJob = jobRepository.save(job);
            execution.setStatus(savedJob.getStatus());
            execution.setDurationMs(durationMs);
            executionRepository.save(execution);

            MDC.put(LoggingConstants.STATUS, savedJob.getStatus().name());
            MDC.put(LoggingConstants.DURATION_MS, String.valueOf(durationMs));

            if (savedJob.getStatus() == JobStatus.COMPLETED) {
                log.info("Job execution completed successfully");
            } else {
                log.warn("Job execution completed with failure");
            }

            publishJobStatusChanged(savedJob);
            MDC.remove(LoggingConstants.JOB_ID);
            MDC.remove(LoggingConstants.EXECUTION_ID);
            MDC.remove(LoggingConstants.USER_ID);
            MDC.remove(LoggingConstants.DURATION_MS);
            MDC.remove(LoggingConstants.STATUS);
        }
    }

    /***---------------JOB STATUS TRANSITIONS--------------***/
    private void completeJob(Job job) {
        job.setStatus(JobStatus.COMPLETED);
        job.setFailureReason(null);
    }

    private void failJob(Job job, String failureReason) {
        job.setStatus(JobStatus.FAILED);
        job.setFailureReason(failureReason);
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
            MDC.put(LoggingConstants.JOB_ID, String.valueOf(job.getId()));
            MDC.put(LoggingConstants.STATUS, JobStatus.QUEUED.name());
            log.warn("Recovered stuck job back to QUEUED");
            MDC.remove(LoggingConstants.JOB_ID);
            MDC.remove(LoggingConstants.STATUS);

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