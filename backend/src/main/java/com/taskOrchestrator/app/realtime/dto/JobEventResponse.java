package com.taskOrchestrator.app.realtime.dto;

import com.taskOrchestrator.app.job.model.JobStatus;
import com.taskOrchestrator.app.realtime.model.EventType;
import com.taskOrchestrator.app.realtime.model.JobEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobEventResponse {
    private Long id;
    private Long jobId;
    private String username;
    private EventType eventType;
    private JobStatus jobStatus;

    public static JobEventResponse fromJobEvent(JobEvent jobEvent) {
        return JobEventResponse.builder()
                .id(jobEvent.getId())
                .jobId(jobEvent.getJobId())
                .username(jobEvent.getUsername())
                .eventType(jobEvent.getEventType())
                .jobStatus(jobEvent.getStatus())
                .build();
    }
}