package com.hrmanproject.domain.employee.dto;

import com.hrmanproject.domain.employee.enums.EmployeeTypes;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ManagerEmployeeViewDto {

    private Long id;
    private String name;
    private String mail;
    private String phoneNum;
    private EmployeeTypes employeeType;
    private Long departmentId;
    private double baseSalary;
    private double performanceMultiplier;
    private double currentSalary;
    private List<Long> currentProjectIds;
    private List<Long> completedProjectIds;
}
