package com.fedstack.demo.dto;

import com.fedstack.demo.model.Job;
import com.fedstack.demo.model.JobStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobResponse {

    private Long id;
    private String name;
    private String description;
    private JobStatus status;
    private Integer retryCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private String failureReason;
    private LocalDateTime queuedAt;

    public static JobResponse fromJob(Job job) {
        return JobResponse.builder()
                .id(job.getId())
                .name(job.getName())
                .description(job.getDescription())
                .status(job.getStatus())
                .retryCount(job.getRetryCount())
                .createdAt(job.getCreatedAt())
                .updatedAt(job.getUpdatedAt())
                .startedAt(job.getStartedAt())
                .completedAt(job.getCompletedAt())
                .failureReason(job.getFailureReason())
                .queuedAt(job.getQueuedAt())
                .build();
    }
}
