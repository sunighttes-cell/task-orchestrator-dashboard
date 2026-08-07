package com.taskOrchestrator.app.job.service;

import com.taskOrchestrator.app.auth.application.CurrentUserProvider;
import com.taskOrchestrator.app.auth.domain.User;
import com.taskOrchestrator.app.auth.domain.UserRepository;
import com.taskOrchestrator.app.job.dto.CreateJobRequest;
import com.taskOrchestrator.app.job.dto.JobResponse;
import com.taskOrchestrator.app.job.dto.StatusSummary;
import com.taskOrchestrator.app.common.exception.JobNotFoundException;
import com.taskOrchestrator.app.job.model.Job;
import com.taskOrchestrator.app.job.model.JobStatus;
import com.taskOrchestrator.app.job.repository.JobRepository;
import com.taskOrchestrator.app.job.repository.specification.JobSpecification;
import com.taskOrchestrator.app.realtime.model.EventType;
import com.taskOrchestrator.app.realtime.model.JobEvent;
import com.taskOrchestrator.app.realtime.service.JobEventPublisher;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static com.taskOrchestrator.app.realtime.model.EventType.JOB_CREATED;

@Service
//@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;
    private final CurrentUserProvider currentUserProvider;
    private final UserRepository userRepository;
    private final JobEventPublisher jobEventPublisher;

    public JobService(JobRepository jobRepository, CurrentUserProvider currentUserProvider,
                      UserRepository userRepository, JobEventPublisher jobEventPublisher) {
        this.jobRepository = jobRepository;
        this.currentUserProvider = currentUserProvider;
        this.userRepository = userRepository;
        this.jobEventPublisher = jobEventPublisher;
    }

//    public List<Job> getJobsByUsername() {
//        String username = currentUserProvider.getCurrentUser().username();
//        return jobRepository.findByUserUsername(username);
//    }

    private Specification<Job> baseSpec() {
        String username = currentUserProvider.getCurrentUser().username();
        Specification<Job> spec = Specification.unrestricted();
        spec = spec.and(JobSpecification.belongsToUser(username));
        spec = spec.and(JobSpecification.isNotDeleted());
        return spec;
    }

    public JobResponse createJob(CreateJobRequest request) {
        System.out.println("Current currentUserProvider: " + currentUserProvider);
        String username = currentUserProvider.getCurrentUser().username();
        System.out.println("Current user: " + username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        System.out.println("User found: " + user.getId());
        Job job = Job.builder()
                .name(request.name())
                .status(JobStatus.QUEUED)
                .queuedAt(LocalDateTime.now())
                .retryCount(0)
                .deleted(false)
                .failureReason(null)
                .user(user)
                .build();
        //save
        Job savedJob = jobRepository.save(job);
        //publish
        jobEventPublisher.publish(
                JobEvent.fromJob(savedJob, EventType.JOB_CREATED)
        );

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
        spec = spec.and(baseSpec());

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
        Specification<Job> spec = baseSpec()
                .and(JobSpecification.hasId(jobId));

        Job job = jobRepository.findOne(spec)
                .orElseThrow(() -> new JobNotFoundException(jobId));

        if (job.getStatus() != JobStatus.FAILED) {
            throw new IllegalStateException("Job must be in FAILED status to retry");
        }

        job.setStatus(JobStatus.QUEUED);
        job.setRetryCount(job.getRetryCount() + 1);
        job.setStartedAt(null);
        job.setCompletedAt(null);
        job.setUpdatedAt(LocalDateTime.now());

        Job saved = jobRepository.save(job);
        jobEventPublisher.publish(JobEvent.fromJob(saved, EventType.JOB_RETRIED));

        return JobResponse.fromJob(saved);
    }

    public List<StatusSummary> getStatusSummary() {
        String username = currentUserProvider.getCurrentUser().username();
        return jobRepository.getStatusSummaryByUser(username);
    }

    @Transactional
    public void deleteJob(Long id, Pageable pageable) {
        Specification<Job> spec = baseSpec()
                .and(JobSpecification.hasId(id));

        Job job = jobRepository.findOne(spec)
                .orElseThrow(() -> new JobNotFoundException(id));

        if (job.getStatus() == JobStatus.RUNNING) {
            throw new IllegalStateException("Cannot delete a running job");
        }

        job.setDeleted(true);
        jobRepository.save(job);
    }
}
