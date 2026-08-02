package com.hrmanproject.domain.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrmanproject.common.AbstractPostgresIntegrationTest;
import com.hrmanproject.domain.auth.dto.LoginRequestDto;
import com.hrmanproject.domain.employee.dto.EmployeeCreateRequestDto;
import com.hrmanproject.domain.employee.enums.EmployeeTypes;
import com.hrmanproject.domain.employee.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AuthIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmployeeRepository employeeRepository;

    @BeforeEach
    void setUp() {
        employeeRepository.deleteAll();
    }

    @Test
    void shouldLoginSuccessfullyAndReturnRoleMetadata() throws Exception {
        EmployeeCreateRequestDto createRequest = EmployeeCreateRequestDto.builder()
                .name("Alice Manager")
                .mail("alice.manager@company.com")
                .phoneNum("05559998877")
                .employeeType(EmployeeTypes.MANAGER)
                .baseSalary(95000.0)
                .build();

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated());

        LoginRequestDto loginRequest = LoginRequestDto.builder()
                .mail("alice.manager@company.com")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mail", is("alice.manager@company.com")))
                .andExpect(jsonPath("$.managerial", is(true)))
                .andExpect(jsonPath("$.canAssignProjects", is(true)))
                .andExpect(jsonPath("$.assignableTargetRoles", hasItems("SENIOR", "JUNIOR", "ENTRY")));
    }

    @Test
    void shouldFailLoginWhenUserNotFound() throws Exception {
        LoginRequestDto loginRequest = LoginRequestDto.builder()
                .mail("non.existent@company.com")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldFailLoginWhenEmailIsInvalid() throws Exception {
        LoginRequestDto loginRequest = LoginRequestDto.builder()
                .mail("invalid-mail-string")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }
}
