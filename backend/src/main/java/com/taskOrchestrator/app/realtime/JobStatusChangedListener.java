package com.taskOrchestrator.app.realtime;

import com.taskOrchestrator.app.job.events.JobStatusChangedEvent;
import com.taskOrchestrator.app.job.model.Job;
import com.taskOrchestrator.app.realtime.model.EventType;
import com.taskOrchestrator.app.realtime.model.JobEvent;
import com.taskOrchestrator.app.realtime.service.JobEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class JobStatusChangedListener {

    private final JobEventPublisher jobEventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onJobStatusChanged(JobStatusChangedEvent event) {
        log.info("Publishing SSE event: {} for job {}",
                event.job().getStatus(),
                event.job().getId());

        Job job = event.job();

        EventType eventType = switch (job.getStatus()) {
            case RUNNING -> EventType.JOB_STARTED;
            case COMPLETED -> EventType.JOB_COMPLETED;
            case FAILED -> EventType.JOB_FAILED;
            default -> null;
        };

        if (eventType == null) {
            return;
        }

        JobEvent jobEvent = JobEvent.builder()
                .jobId(job.getId())
                .username(job.getUser().getUsername())
                .eventType(eventType)
                .status(job.getStatus())
                .build();

        jobEventPublisher.publish(jobEvent);
    }
}