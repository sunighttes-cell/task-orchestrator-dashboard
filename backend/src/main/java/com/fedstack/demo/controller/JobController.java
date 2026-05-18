package com.fedstack.demo.controller;

import ch.qos.logback.classic.Logger;
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
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.Console;
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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJob(@PathVariable Long id) {
        Pageable p = PageRequest.of(0, 100);
        jobService.deleteJob(id, p);
        return ResponseEntity.noContent().build();
    }
}
