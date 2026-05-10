package com.fedstack.demo.service;

import com.fedstack.demo.dto.JobResponse;
import com.fedstack.demo.model.Job;
import com.fedstack.demo.model.JobStatus;
import com.fedstack.demo.repository.JobRepository;
import com.fedstack.demo.repository.specification.JobSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

//Simulate orchestration: processes jobs and orchestrates execution.
//separates orchestration from REST API logic
//controllers should orchestrate requests, services orchestrate business logic
@Service
@RequiredArgsConstructor
@Slf4j
public class JobProcessorService {
    private final JobRepository jobRepository;
    private final JobService jobService;

    //polling method: every 5secs - poll db, find queued jobs, process them
    @Scheduled(fixedDelay = 5000)
    public void processQueuedJobs() {
        List<Job> queuedJobs =
                jobRepository.findTop5ByStatusOrderByCreatedAtAsc(JobStatus.QUEUED);

        for (Job job : queuedJobs) {
            processJob(job);
        }
    }

    private void processJob(Job job) {
        try {
            job.setStatus(JobStatus.RUNNING);
            job.setStartedAt(LocalDateTime.now());
            jobRepository.save(job);
            log.info("Processing job id = {} name = {}", job.getId(), job.getName());

            //simulates async workloads, long-running tasks, external APIs etc
            Thread.sleep(2000);

            //random outcome generator: set to 70% success, 30% failure
            //simulating real orchestration systems: transient failures, unstable distributed systems
            boolean success = Math.random() > 0.3;

            if (success) {
                job.setStatus(JobStatus.COMPLETED);
                log.info("Job completed successfully id={}", job.getId());
            } else {
                job.setStatus(JobStatus.FAILED);
                job.setFailureReason("Job failed due to random failure");
                log.error("Job failed id={}", job.getId());
            }
            job.setCompletedAt(LocalDateTime.now());
            jobRepository.save(job);
            log.info("Job processing completed for job id: {} status: {}", job.getId(), job.getStatus());
        }
         catch (Exception e) {
            log.error("Unexpected processing error:", e);
             job.setStatus(JobStatus.FAILED);
             job.setFailureReason(e.getMessage());
             job.setCompletedAt(LocalDateTime.now());
             jobRepository.save(job);
        }
    }
}


