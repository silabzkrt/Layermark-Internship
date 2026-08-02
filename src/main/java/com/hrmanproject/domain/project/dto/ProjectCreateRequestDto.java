package com.hrmanproject.domain.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectCreateRequestDto {

    @NotBlank(message = "Project name cannot be empty")
    private String name;

    private String description;

    @Positive(message = "Target days must be positive")
    private int targetDays;
}
