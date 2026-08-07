//Represents event payload - what an event looks like
package com.taskOrchestrator.app.realtime.model;

import com.taskOrchestrator.app.job.model.Job;
import com.taskOrchestrator.app.job.model.JobStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "realtime")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long jobId;

    @Column(nullable = false)
    private String username;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobStatus status;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime timeStamp;

    public static JobEvent fromJob(Job job, EventType eventType) {
        return JobEvent.builder()
                .jobId(job.getId())
                .username(job.getUser().getUsername())
                .eventType(eventType)
                .status(job.getStatus())
                .build();
    }
}