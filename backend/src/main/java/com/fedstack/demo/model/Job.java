package com.fedstack.demo.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

//Add Base Entity Annotations
@Entity
@Table(name = "jobs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Job {

//primary key
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;

//core fields
@NotBlank
@Size(max = 100)
@Column(nullable = false, length = 100)
private String name;

@Size(max = 500)
private String description;

//status Field
@Enumerated(EnumType.STRING)
//using EnumType.STRING instead of ORDINAL
//String enum mapping prevents data corruption when enum ordering changes.
@Column(nullable = false)
private JobStatus status;

//retry count for tracking retry, metrics and future limits
    @Builder.Default
    @Column(nullable = false)
    private Integer retryCount = 0;

//timestamps
@CreationTimestamp
private LocalDateTime createdAt;

@UpdateTimestamp
private LocalDateTime updatedAt;

//not automated controlled by processor service
private LocalDateTime startedAt;
private LocalDateTime completedAt;

//default status set inside service layer bc business logic belongs in service
}
