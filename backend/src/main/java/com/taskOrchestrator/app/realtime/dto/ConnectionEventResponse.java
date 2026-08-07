package com.taskOrchestrator.app.realtime.dto;

public record ConnectionEventResponse(
        String type,
        String message
) {}