package com.fedstack.demo.service;

import com.fedstack.demo.dto.DashboardMetricsResponse;
import com.fedstack.demo.model.JobStatus;
import com.fedstack.demo.repository.ExecutionRepository;
import com.fedstack.demo.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final JobRepository jobRepository;
    private final ExecutionRepository executionRepository;

    public DashboardMetricsResponse getMetrics() {
        long totalJobs = jobRepository.count();
        long completedJobs = jobRepository.countByStatus(JobStatus.COMPLETED);
        long runningJobs = jobRepository.countByStatus(JobStatus.RUNNING);
        long failedJobs = jobRepository.countByStatus(JobStatus.FAILED);
        double successRate = totalJobs == 0 ? 0 : (completedJobs * 100.0) / totalJobs;
        Double avgExecutionTime = executionRepository.averageExecutionTime();

        double avgExecutionTimeSeconds = avgExecutionTime == null ? 0 : avgExecutionTime / 1000.0;

        return new DashboardMetricsResponse(
                totalJobs,
                completedJobs,
                runningJobs,
                failedJobs,
                successRate,
                runningJobs,
                avgExecutionTimeSeconds
        );
    }
}
