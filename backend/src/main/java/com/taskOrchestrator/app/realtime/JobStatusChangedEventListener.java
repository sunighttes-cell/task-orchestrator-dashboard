package com.taskOrchestrator.app.realtime;

import com.taskOrchestrator.app.common.logging.LoggingConstants;
import com.taskOrchestrator.app.job.model.Job;
import com.taskOrchestrator.app.realtime.events.JobStatusChangedEvent;
import com.taskOrchestrator.app.realtime.model.EventType;
import com.taskOrchestrator.app.realtime.model.JobEvent;
import com.taskOrchestrator.app.realtime.service.JobEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
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
        String previousJobId = MDC.get(LoggingConstants.JOB_ID);
        String previousStatus = MDC.get(LoggingConstants.STATUS);

        try {
            MDC.put(LoggingConstants.JOB_ID, String.valueOf(job.getId()));
            MDC.put(LoggingConstants.STATUS, job.getStatus().name());
            log.info("Publishing SSE event after transaction commit");

            jobEventPublisher.publish(
                    JobEvent.builder()
                            .jobId(job.getId())
                            .username(
                                    job.getUser().getUsername()
                            )
                            .eventType(resolveEventType(job))
                            .status(job.getStatus())
                            .build()
            );

        } finally {
            restoreMdcValue(LoggingConstants.JOB_ID, previousJobId);
            restoreMdcValue(LoggingConstants.STATUS, previousStatus);
        }
    }

    private void restoreMdcValue(String key, String previousValue) {
        if (previousValue == null) {
            MDC.remove(key);
        } else {
            MDC.put(key, previousValue);
        }
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