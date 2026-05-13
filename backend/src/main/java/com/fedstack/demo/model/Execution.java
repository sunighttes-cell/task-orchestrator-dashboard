//execution entity
package com.fedstack.demo.model;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "executions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Execution {
    //primary key
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long executionId;
    private Long id;
    @ManyToOne
    @JoinColumn(name = "job_id")
    private Job job;

    @Enumerated(EnumType.STRING)
    private JobStatus status;

    private Long durationMs;
}
