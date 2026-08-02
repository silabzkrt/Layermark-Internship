package com.hrmanproject.domain.auth.service;

import com.hrmanproject.domain.auth.dto.LoginRequestDto;
import com.hrmanproject.domain.auth.dto.LoginResponseDto;
import com.hrmanproject.domain.employee.entity.Employee;
import com.hrmanproject.domain.employee.enums.EmployeeTypes;
import com.hrmanproject.domain.employee.repository.EmployeeRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AuthService {

    private final EmployeeRepository employeeRepository;

    public AuthService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public LoginResponseDto login(LoginRequestDto request) {
        Employee employee = employeeRepository.findByMail(request.getMail())
                .orElseThrow(() -> new EntityNotFoundException("User not found with mail: " + request.getMail()));

        EmployeeTypes type = employee.getEmployeeType();
        List<EmployeeTypes> assignableRoles = Arrays.stream(EmployeeTypes.values())
                .filter(type::canAssignProjectTo)
                .collect(Collectors.toList());

        return LoginResponseDto.builder()
                .id(employee.getId())
                .name(employee.getName())
                .mail(employee.getMail())
                .employeeType(type)
                .managerial(type.isManagerial())
                .canAssignProjects(!assignableRoles.isEmpty())
                .assignableTargetRoles(assignableRoles)
                .build();
    }
}
