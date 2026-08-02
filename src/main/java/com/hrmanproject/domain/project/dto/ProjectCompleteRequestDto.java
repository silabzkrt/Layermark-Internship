package com.hrmanproject.domain.project.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectCompleteRequestDto {

    @Positive(message = "Actual days must be positive")
    private int actualDays;

    @DecimalMin(value = "0.0", message = "Completion rate must be at least 0.0")
    @DecimalMax(value = "1.0", message = "Completion rate cannot exceed 1.0")
    private double completionRate;
}
