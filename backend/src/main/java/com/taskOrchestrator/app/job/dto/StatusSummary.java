package com.taskOrchestrator.app.job.dto;

import com.taskOrchestrator.app.job.model.JobStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatusSummary {
    private JobStatus status;
    private Long count;
}
