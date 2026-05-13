package com.fedstack.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardMetricsResponse {
    private long totalJobs;
    private long completedJobs;
    private long runningJobs;
    private long failedJobs;
    private double successRate;
    private long activeWorkers;
    private double avgExecutionTime;
}