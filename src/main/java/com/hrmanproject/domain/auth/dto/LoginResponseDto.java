package com.hrmanproject.domain.auth.dto;

import com.hrmanproject.domain.auth.enums.EmployeeTypes;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponseDto extends BaseAuthDto {

    private Long id;
    private String name;
    private String mail;
    private EmployeeTypes employeeType;
    private boolean managerial;
    private boolean canAssignProjects;
    private List<EmployeeTypes> assignableTargetRoles;
}
