package com.hrmanproject.domain.employee.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectAssignmentRequestDto {

    @NotNull(message = "Assigner employee id cannot be null")
    private Long assignerId;

    @NotNull(message = "Target employee id cannot be null")
    private Long targetEmployeeId;

    @NotNull(message = "Project id cannot be null")
    private Long projectId;
}
