package com.fedstack.demo.service;

import com.fedstack.demo.dto.CreateJobRequest;
import com.fedstack.demo.dto.JobResponse;
import com.fedstack.demo.dto.StatusSummary;
import com.fedstack.demo.exception.JobNotFoundException;
import com.fedstack.demo.model.Job;
import com.fedstack.demo.model.JobStatus;
import com.fedstack.demo.repository.JobRepository;
import com.fedstack.demo.repository.specification.JobSpecification;
import jakarta.transaction.Transactional;
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
                .deleted(false)
                .failureReason(null)
                .build();
        Job savedJob = jobRepository.save(job);
        return JobResponse.fromJob(savedJob);
    }

    public Page<JobResponse> getJobs(
            JobStatus status,
            String search,
            LocalDateTime createdAfter,
            Integer retryCount,
            Pageable pageable
    ) {
        Specification<Job> spec = Specification.unrestricted();
        spec = spec.and(JobSpecification.isNotDeleted());

        if (status != null) {
            spec = spec.and(JobSpecification.hasStatus(status));
        }

        if (search != null && !search.isBlank()) {
            spec = spec.and(JobSpecification.search(search.trim()));
        }

        if (retryCount != null) {
            spec = spec.and(JobSpecification.hasRetryCount(retryCount));
        }

        if (createdAfter != null) {
            spec = spec.and(JobSpecification.createdAfter(createdAfter));
        }

        //filters, paginates, sorts
        Page<Job> jobs = jobRepository.findAll(spec, pageable);
        return jobs.map(JobResponse::fromJob);
    }

    public JobResponse retryJob(Long jobId) {
        Job job = jobRepository.findById(jobId).orElseThrow(() -> new JobNotFoundException(jobId));
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

    @Transactional
    public void deleteJob(Long id, Pageable pageable) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new JobNotFoundException(id));

        // prevent deleting running jobs
        if (job.getStatus() == JobStatus.RUNNING) {
            throw new IllegalStateException("Cannot delete a running job");
        }

        //soft delete
        job.setDeleted(true);
        jobRepository.save(job);
    }
}
