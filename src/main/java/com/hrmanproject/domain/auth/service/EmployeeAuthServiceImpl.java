package com.hrmanproject.domain.auth.service;

import com.hrmanproject.domain.auth.dto.LoginRequestDto;
import com.hrmanproject.domain.auth.dto.LoginResponseDto;
import com.hrmanproject.domain.employee.entity.Employee;
import com.hrmanproject.domain.employee.enums.EmployeeTypes;
import com.hrmanproject.domain.employee.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class EmployeeAuthServiceImpl extends AbstractAuthService {

    private final EmployeeRepository employeeRepository;

    public EmployeeAuthServiceImpl(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    public LoginResponseDto login(LoginRequestDto request) {
        logger.info("Attempting login for mail: {}", request.getMail());
        Employee employee = employeeRepository.findByMail(request.getMail()).orElse(null);
        validateAccountStatus(employee, request.getMail());

        EmployeeTypes type = employee.getEmployeeType();
        List<EmployeeTypes> assignableRoles = resolveAssignableRoles(type);

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

    @Override
    public boolean authenticateMail(String mail) {
        return employeeRepository.existsByMail(mail);
    }
}
