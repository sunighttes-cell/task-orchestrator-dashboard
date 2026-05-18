package com.fedstack.demo.service;

import com.fedstack.demo.model.Execution;
import com.fedstack.demo.model.Job;
import com.fedstack.demo.model.JobStatus;
import com.fedstack.demo.repository.ExecutionRepository;
import com.fedstack.demo.repository.JobRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

//Simulate orchestration: processes jobs and orchestrates execution.
//separates orchestration from REST API logic
//controllers should orchestrate requests, services orchestrate business logic
@Service
@Slf4j
public class JobProcessorService {
    private static final Duration STUCK_JOB_THRESHOLD = Duration.ofSeconds(60);

    private final JobRepository jobRepository;
    private final ExecutionRepository executionRepository;
    private final JobProcessorService self;

    public JobProcessorService(
            JobRepository jobRepository,
            ExecutionRepository executionRepository,
            @Lazy @Autowired JobProcessorService self
    ) {
        this.jobRepository = jobRepository;
        this.executionRepository = executionRepository;
        this.self = self;
    }

    //polling method: every 5secs - poll db, find queued jobs, process them
    @Transactional
    public void processJob(Long jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow();

        if (job.getStatus() != JobStatus.RUNNING) {
            log.warn("Skipping job {} — not RUNNING", jobId);
            return;
        }

        LocalDateTime startedAt = job.getStartedAt();

        try {
            log.info("Processing job id={}", jobId);

            Thread.sleep(2000);

            boolean success = Math.random() > 0.3;

            if (success) {
                job.setStatus(JobStatus.COMPLETED);
            } else {
                job.setStatus(JobStatus.FAILED);
                job.setFailureReason("Random failure");
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            job.setStatus(JobStatus.FAILED);
            job.setFailureReason("Interrupted");
        } catch (Exception e) {
            job.setStatus(JobStatus.FAILED);
            job.setFailureReason(e.getMessage());
        }

        LocalDateTime completedAt = LocalDateTime.now();
        job.setCompletedAt(completedAt);

        jobRepository.save(job);

        recordExecution(job, startedAt, completedAt);

        log.info("Finished job id={} status={}", jobId, job.getStatus());
    }

    private void recordExecution(Job job, LocalDateTime startedAt, LocalDateTime completedAt) {
        long durationMs = Duration.between(startedAt, completedAt).toMillis();
        Execution execution = Execution.builder()
                .job(job)
                .status(job.getStatus())
                .durationMs(durationMs)
                .build();
        executionRepository.save(execution);
    }

    @Scheduled(fixedDelay = 20000)
    @Transactional
    public void recoverStuckJobsPeriodically() {
        LocalDateTime cutoff = LocalDateTime.now().minus(STUCK_JOB_THRESHOLD);
        List<Job> running = jobRepository.findByStatus(JobStatus.RUNNING);

        List<Job> stuck = new ArrayList<>();
        for (Job job : running) {
            LocalDateTime startedAt = job.getStartedAt();
            // only recover jobs that are actually stuck — not ones still being processed
            if (startedAt == null || startedAt.isBefore(cutoff)) {
                job.setStatus(JobStatus.QUEUED);
                job.setStartedAt(null);
                stuck.add(job);
            }
        }

        if (stuck.isEmpty()) {
            return;
        }
        jobRepository.saveAll(stuck);
        log.warn("{} stuck job(s) (running > {}s) recovered to QUEUED", stuck.size(), STUCK_JOB_THRESHOLD.getSeconds());
    }

    @Async
    public void processJobAsync(Long jobId) {
        processJob(jobId);
    }

    @Transactional
    public List<Job> claimNextJobs(int limit) {
        List<Job> jobs = jobRepository.findNextJobsForUpdate(
                JobStatus.QUEUED,
                PageRequest.of(0, limit)
        );

        if (jobs.isEmpty()) return jobs;

        LocalDateTime now = LocalDateTime.now();

        for (Job job : jobs) {
            job.setStatus(JobStatus.RUNNING);
            job.setStartedAt(now);
        }

        return jobRepository.saveAll(jobs);
    }

    @Scheduled(fixedDelay = 5000)
    public void processQueuedJobs() {
        List<Job> jobs = self.claimNextJobs(5);

        for (Job job : jobs) {
            // route through the proxy so @Async actually applies (self-invocation skips AOP)
            self.processJobAsync(job.getId());
        }
    }
}


