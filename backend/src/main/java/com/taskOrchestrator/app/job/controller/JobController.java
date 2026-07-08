package com.taskOrchestrator.app.job.controller;

import com.taskOrchestrator.app.job.dto.CreateJobRequest;
import com.taskOrchestrator.app.job.dto.DashboardMetricsResponse;
import com.taskOrchestrator.app.job.dto.JobResponse;
import com.taskOrchestrator.app.job.dto.StatusSummary;
import com.taskOrchestrator.app.job.model.JobStatus;
import com.taskOrchestrator.app.job.service.DashboardService;
import com.taskOrchestrator.app.job.service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;
    private final DashboardService dashboardService;

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @PostMapping
    public JobResponse createJob(@RequestBody CreateJobRequest request) {
        return jobService.createJob(request);
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping
    public ResponseEntity<Page<JobResponse>> getJobs(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) LocalDateTime createdAt,
            @RequestParam(required = false) Integer retryCount,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        size = Math.min(size, 100);

        JobStatus parsedStatus = null;

        if (status != null) {
            try {
                parsedStatus = JobStatus.valueOf(status);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().build();
            }
        }

        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<JobResponse> jobs =
                jobService.getJobs(parsedStatus, search, createdAt, retryCount, pageable);

        return ResponseEntity.ok(jobs);
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @PostMapping("/{id}/retry")
    public ResponseEntity<JobResponse> retryJob(@PathVariable Long id) {
        JobResponse job = jobService.retryJob(id);
        return ResponseEntity.ok(job);
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping("/status-summary")
    public ResponseEntity<List<StatusSummary>> getStatusSummary() {
        List<StatusSummary> summary = jobService.getStatusSummary();
        return ResponseEntity.ok(summary);
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping("/dashboard-metrics")
    public ResponseEntity<DashboardMetricsResponse> getMetrics() {
        DashboardMetricsResponse metrics = dashboardService.getMetrics();
        return ResponseEntity.ok(metrics);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJob(@PathVariable Long id) {
        Pageable p = PageRequest.of(0, 100);
        jobService.deleteJob(id, p);
        return ResponseEntity.noContent().build();
    }
}
