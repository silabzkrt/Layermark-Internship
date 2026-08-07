package com.hrmanproject.domain.auth.service;

import com.hrmanproject.domain.auth.dto.LoginRequestDto;
import com.hrmanproject.domain.auth.dto.LoginResponseDto;
import com.hrmanproject.domain.auth.enums.EmployeeTypes;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class GenericAuthServiceImpl extends AbstractAuthService {

    private final JdbcTemplate jdbcTemplate;

    public GenericAuthServiceImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public LoginResponseDto login(LoginRequestDto request) {
        logger.info("Attempting login for mail: {}", request.getMail());
        
        // Dynamically query the employee table
        String sql = "SELECT * FROM employee WHERE mail = ?";
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, request.getMail());
        
        Map<String, Object> employee = results.isEmpty() ? null : results.get(0);
        validateAccountStatus(employee, request.getMail());

        String typeStr = (String) employee.get("employee_type");
        EmployeeTypes type = typeStr != null ? EmployeeTypes.valueOf(typeStr) : EmployeeTypes.NULL;
        List<EmployeeTypes> assignableRoles = resolveAssignableRoles(type);

        Number idNumber = (Number) employee.get("id");
        Long id = idNumber != null ? idNumber.longValue() : null;

        return LoginResponseDto.builder()
                .id(id)
                .name((String) employee.get("name"))
                .mail((String) employee.get("mail"))
                .employeeType(type)
                .managerial(type.isManagerial())
                .canAssignProjects(!assignableRoles.isEmpty())
                .assignableTargetRoles(assignableRoles)
                .build();
    }

    @Override
    public boolean authenticateMail(String mail) {
        String sql = "SELECT COUNT(*) FROM employee WHERE mail = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, mail);
        return count != null && count > 0;
    }
}
