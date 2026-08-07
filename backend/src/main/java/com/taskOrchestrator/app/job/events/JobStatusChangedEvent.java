package com.taskOrchestrator.app.job.events;

import com.taskOrchestrator.app.job.model.Job;

public record JobStatusChangedEvent(Job job) {
}