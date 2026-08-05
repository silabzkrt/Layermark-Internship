package com.hrmanproject.domain.auth.service;

import com.hrmanproject.domain.auth.dto.LoginRequestDto;
import com.hrmanproject.domain.auth.dto.LoginResponseDto;
import java.util.Map;
import com.hrmanproject.domain.auth.enums.EmployeeTypes;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractAuthService implements AuthService {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected void validateAccountStatus(Map<String, Object> employee, String mail) {
        if (employee == null || employee.isEmpty()) {
            logger.warn("Authentication failed: Account not found for mail {}", mail);
            throw new EntityNotFoundException("User not found with mail: " + mail);
        }
    }

    protected List<EmployeeTypes> resolveAssignableRoles(EmployeeTypes type) {
        if (type == null) {
            return List.of();
        }
        return Arrays.stream(EmployeeTypes.values())
                .filter(type::canAssignProjectTo)
                .collect(Collectors.toList());
    }

    @Override
    public abstract LoginResponseDto login(LoginRequestDto request);

    @Override
    public abstract boolean authenticateMail(String mail);
}
