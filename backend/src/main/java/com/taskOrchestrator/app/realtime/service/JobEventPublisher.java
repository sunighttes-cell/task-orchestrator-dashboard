package com.taskOrchestrator.app.realtime.service;

import com.taskOrchestrator.app.realtime.dto.ConnectionEventResponse;
import com.taskOrchestrator.app.realtime.model.JobEvent;
import com.taskOrchestrator.app.realtime.dto.JobEventResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class JobEventPublisher {
    private static final long SSE_TIMEOUT = 30 * 60 * 1000L;
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(String username) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        emitters.put(username, emitter);

        emitter.onCompletion(() -> removeEmitter(username, emitter));
        emitter.onTimeout(() -> removeEmitter(username, emitter));
        emitter.onError(error -> removeEmitter(username, emitter));
//        sendConnectionEvent(emitter);

        return emitter;
    }

    public void publish(JobEvent event) {
        SseEmitter emitter = emitters.get(event.getUsername());
        if (emitter == null) { return; }

        try {
            JobEventResponse response = JobEventResponse.fromJobEvent(event);
            emitter.send(
                    SseEmitter.event()
                            .name(event.getEventType().name())
                            .data(response)
            );
        } catch (IOException exception) {
            removeEmitter(event.getUsername(), emitter);
        }
    }

    private void sendConnectionEvent(SseEmitter emitter) {
        try {
            ConnectionEventResponse response = new ConnectionEventResponse(
                    "CONNECTED",
                    "SSE connection established"
            );

            emitter.send(
                    SseEmitter.event()
                            .name("CONNECTED")
                            .data(response)
            );
        } catch (IOException exception) {
            emitter.completeWithError(exception);
        }
    }

    private void removeEmitter(String username, SseEmitter emitter) {
        emitters.remove(username, emitter);
    }
}