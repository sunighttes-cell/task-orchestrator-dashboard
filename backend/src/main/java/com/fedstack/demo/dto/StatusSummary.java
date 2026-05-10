package com.fedstack.demo.dto;

import com.fedstack.demo.model.JobStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatusSummary {
    private JobStatus status;
    private Long count;
}
