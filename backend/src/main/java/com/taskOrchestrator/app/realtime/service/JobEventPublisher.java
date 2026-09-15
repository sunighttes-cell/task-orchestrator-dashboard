package com.taskOrchestrator.app.realtime.service;

import com.taskOrchestrator.app.common.logging.LoggingConstants;
import com.taskOrchestrator.app.realtime.dto.ConnectionEventResponse;
import com.taskOrchestrator.app.realtime.model.JobEvent;
import com.taskOrchestrator.app.realtime.dto.JobEventResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;


import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class JobEventPublisher {
    private static final long SSE_TIMEOUT = 30 * 60 * 1000L;
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(String username) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        emitters.put(username, emitter);
        emitter.onCompletion(() -> removeEmitter(username, emitter));
        emitter.onTimeout(() -> removeEmitter(username, emitter));
        emitter.onError(error -> removeEmitter(username, emitter));
        log.info("SSE subscriber connected");

        return emitter;
    }

    public void publish(JobEvent event) {
        SseEmitter emitter = emitters.get(event.getUsername());

        if (emitter == null) {
            log.debug("No active SSE subscriber for job event");
            return;
        }

        String previousJobId = MDC.get(LoggingConstants.JOB_ID);
        String previousStatus = MDC.get(LoggingConstants.STATUS);

        try {
            MDC.put(LoggingConstants.JOB_ID, String.valueOf(event.getJobId()));
            MDC.put(LoggingConstants.STATUS, event.getStatus().name());
            JobEventResponse response = JobEventResponse.fromJobEvent(event);
            emitter.send(SseEmitter.event()
                            .name(event.getEventType().name())
                            .data(response)
            );

            log.info("SSE job event published");

        } catch (IOException exception) {
            log.warn("Failed to publish SSE job event", exception);
            removeEmitter(event.getUsername(), emitter);

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

    private void sendConnectionEvent(SseEmitter emitter) {
        try {
            ConnectionEventResponse response = new ConnectionEventResponse(
                    "CONNECTED",
                    "SSE connection established"
            );

            emitter.send(SseEmitter.event()
                            .name("CONNECTED")
                            .data(response)
            );
        } catch (IOException exception) {
            emitter.completeWithError(exception);
        }
    }

    private void removeEmitter(String username, SseEmitter emitter) {
        emitters.remove(username, emitter);
        log.debug("SSE subscriber disconnected");
    }
}