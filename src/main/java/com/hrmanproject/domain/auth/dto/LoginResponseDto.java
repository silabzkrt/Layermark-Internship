package com.hrmanproject.domain.auth.dto;

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
public class LoginResponseDto {

    private Long id;
    private String name;
    private String mail;
    private EmployeeTypes employeeType;
    private boolean managerial;
    private boolean canAssignProjects;
    private List<EmployeeTypes> assignableTargetRoles;
}
