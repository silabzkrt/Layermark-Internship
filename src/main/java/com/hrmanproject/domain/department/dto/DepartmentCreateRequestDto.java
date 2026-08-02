package com.hrmanproject.domain.department.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartmentCreateRequestDto {

    @NotBlank(message = "Department name cannot be empty")
    private String name;

    @NotBlank(message = "Department code cannot be empty")
    private String code;
}
