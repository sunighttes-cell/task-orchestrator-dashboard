package com.taskOrchestrator.app.realtime;

import com.taskOrchestrator.app.job.model.Job;
import com.taskOrchestrator.app.realtime.events.JobStatusChangedEvent;
import com.taskOrchestrator.app.realtime.model.EventType;
import com.taskOrchestrator.app.realtime.model.JobEvent;
import com.taskOrchestrator.app.realtime.service.JobEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class JobStatusChangedEventListener {

    private final JobEventPublisher jobEventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleJobStatusChanged(JobStatusChangedEvent event) {

        Job job = event.job();

        log.info(
                "Publishing SSE event after transaction commit: jobId={}, status={}",
                job.getId(),
                job.getStatus()
        );

        jobEventPublisher.publish(
                JobEvent.builder()
                        .jobId(job.getId())
                        .username(job.getUser().getUsername())
                        .eventType(resolveEventType(job))
                        .status(job.getStatus())
                        .build()
        );
    }

    private EventType resolveEventType(Job job) {
        return switch (job.getStatus()) {
            case QUEUED -> EventType.JOB_CREATED;
            case RUNNING -> EventType.JOB_STARTED;
            case COMPLETED -> EventType.JOB_COMPLETED;
            case FAILED -> EventType.JOB_FAILED;
        };
    }
}