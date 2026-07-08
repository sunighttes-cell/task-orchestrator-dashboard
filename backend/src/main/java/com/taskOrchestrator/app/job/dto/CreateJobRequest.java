package com.taskOrchestrator.app.job.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
public record CreateJobRequest(
        String name
) {}
//
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class CreateJobRequest {
//
//    @NotBlank
//    @Size(max = 100)
//    private String name;
//
//    @Size(max = 500)
//    private String description;
//}
