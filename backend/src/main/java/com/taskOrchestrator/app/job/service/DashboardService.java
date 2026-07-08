package com.taskOrchestrator.app.job.service;

import com.taskOrchestrator.app.auth.application.CurrentUserProvider;
import com.taskOrchestrator.app.job.dto.DashboardMetricsResponse;
import com.taskOrchestrator.app.job.model.JobStatus;
import com.taskOrchestrator.app.job.repository.ExecutionRepository;
import com.taskOrchestrator.app.job.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final JobRepository jobRepository;
    private final ExecutionRepository executionRepository;
    private final CurrentUserProvider currentUserProvider;

    public DashboardMetricsResponse getMetrics() {
        String username = currentUserProvider.getCurrentUser().username();
        System.out.println("Current user: " + username);
        long totalJobs = jobRepository.countActiveByUser(username);
        long completedJobs = jobRepository.countActiveByUserAndStatus(JobStatus.COMPLETED, username);
        long runningJobs = jobRepository.countActiveByUserAndStatus(JobStatus.RUNNING, username);
        long failedJobs = jobRepository.countActiveByUserAndStatus(JobStatus.FAILED, username);
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
