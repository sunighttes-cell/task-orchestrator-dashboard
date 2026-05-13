package com.fedstack.demo.controller;

import com.fedstack.demo.dto.CreateJobRequest;
import com.fedstack.demo.dto.DashboardMetricsResponse;
import com.fedstack.demo.dto.JobResponse;
import com.fedstack.demo.dto.StatusSummary;
import com.fedstack.demo.model.JobStatus;
import com.fedstack.demo.service.DashboardService;
import com.fedstack.demo.service.JobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;
    private final DashboardService dashboardService;

    @PostMapping
    public ResponseEntity<JobResponse> createJob(@Valid @RequestBody CreateJobRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(jobService.createJob(request));
    }

    @GetMapping
    public ResponseEntity<Page<JobResponse>> getJobs(
            @RequestParam(required = false) JobStatus status,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) LocalDateTime createdAt,
            @RequestParam(required = false) Integer retryCount,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<JobResponse> jobs = jobService.getJobs(status, search, createdAt, retryCount, pageable);
        return ResponseEntity.ok(jobs);
    }

    @PostMapping("/{id}/retry")
    public ResponseEntity<JobResponse> retryJob(@PathVariable Long id) {
        JobResponse job = jobService.retryJob(id);
        return ResponseEntity.ok(job);
    }

    @GetMapping("/status-summary")
    public ResponseEntity<List<StatusSummary>> getStatusSummary() {
        List<StatusSummary> summary = jobService.getStatusSummary();
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/dashboard-metrics")
    public ResponseEntity<DashboardMetricsResponse> getMetrics() {
        DashboardMetricsResponse metrics = dashboardService.getMetrics();
        return ResponseEntity.ok(metrics);
    }
}
