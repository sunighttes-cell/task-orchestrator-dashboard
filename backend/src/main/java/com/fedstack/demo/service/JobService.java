package com.fedstack.demo.service;

import com.fedstack.demo.dto.CreateJobRequest;
import com.fedstack.demo.dto.JobResponse;
import com.fedstack.demo.dto.StatusSummary;
import com.fedstack.demo.model.Job;
import com.fedstack.demo.model.JobStatus;
import com.fedstack.demo.repository.JobRepository;
import com.fedstack.demo.repository.specification.JobSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;

    public JobResponse createJob(CreateJobRequest request) {
        Job job = Job.builder()
                .name(request.getName())
                .description(request.getDescription())
                .status(JobStatus.QUEUED)
                .queuedAt(LocalDateTime.now())
                .retryCount(0)
                .build();
        Job savedJob = jobRepository.save(job);
        return JobResponse.fromJob(savedJob);
    }

    public Page<JobResponse> getJobs(JobStatus status, String search, LocalDateTime createdAfter, Integer retryCount, Pageable pageable) {
        Specification<Job> spec = Specification.allOf();
        if (status != null) {
            spec = spec.and(JobSpecification.hasStatus(status));
        }
        if (search != null && !search.trim().isEmpty()) {
            spec = spec.and(JobSpecification.search(search));
        }
        //Retry Count Filter
        if (retryCount != null) {
            spec = spec.and(JobSpecification.hasRetryCount(retryCount));
        }
        //Date Range Filter
        if (createdAfter != null) {
            spec = spec.and(JobSpecification.createdAfter(createdAfter));
        }
        //Running Jobs Only
        if (status == JobStatus.RUNNING) {
            spec = spec.and(JobSpecification.hasStatus(JobStatus.RUNNING));
        }

        //filters, paginates, sorts all automatically
        Page<Job> jobs = jobRepository.findAll(spec, pageable);
        return jobs.map(JobResponse::fromJob);
    }

    public JobResponse retryJob(Long jobId) {
        Job job = jobRepository.findById(jobId).orElseThrow(() -> new RuntimeException("Job not found"));
        if (job.getStatus() != JobStatus.FAILED) {
            throw new IllegalStateException("Job must be in FAILED status to retry");
        }
        job.setStatus(JobStatus.QUEUED);
        job.setRetryCount(job.getRetryCount() + 1);
        job.setStartedAt(null);
        job.setCompletedAt(null);
        job.setUpdatedAt(LocalDateTime.now());
        Job saved = jobRepository.save(job);
        return JobResponse.fromJob(saved);
    }

    public List<StatusSummary> getStatusSummary() {
        return jobRepository.getStatusSummary();
    }
}