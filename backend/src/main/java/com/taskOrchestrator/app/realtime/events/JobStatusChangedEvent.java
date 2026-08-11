package com.taskOrchestrator.app.realtime.events;

import com.taskOrchestrator.app.job.model.Job;

public record JobStatusChangedEvent(Job job) {
}