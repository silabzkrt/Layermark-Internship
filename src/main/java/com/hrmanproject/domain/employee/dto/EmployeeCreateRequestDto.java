package com.hrmanproject.domain.employee.dto;

import com.hrmanproject.domain.employee.enums.EmployeeTypes;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeCreateRequestDto {

    @NotBlank(message = "Name cannot be empty")
    private String name;

    @Email(message = "Enter a valid mail")
    @NotBlank(message = "Email cannot be empty")
    private String mail;

    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Enter a valid phone number")
    @NotBlank(message = "Phone number cannot be empty")
    private String phoneNum;

    @NotNull(message = "Employee type is required")
    private EmployeeTypes employeeType;

    @PositiveOrZero(message = "Base salary cannot be negative")
    private double baseSalary;

    private Long departmentId;
}
