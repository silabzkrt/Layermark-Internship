package com.hrmanproject.domain.employee;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrmanproject.common.AbstractPostgresIntegrationTest;
import com.hrmanproject.common.search.SearchCriteria;
import com.hrmanproject.common.search.SearchOperator;
import com.hrmanproject.domain.employee.dto.EmployeeCreateRequestDto;
import com.hrmanproject.domain.employee.enums.EmployeeTypes;
import com.hrmanproject.domain.employee.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class EmployeeSearchIntegrationTest extends AbstractPostgresIntegrationTest {

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
    void shouldSearchEmployeesByRoleAndSalaryGreaterThan() throws Exception {
        createEmployee("Alice Senior", "alice@company.com", "05551000001", EmployeeTypes.SENIOR, 60000.0);
        createEmployee("Bob Senior", "bob@company.com", "05551000002", EmployeeTypes.SENIOR, 40000.0);
        createEmployee("Charlie Junior", "charlie@company.com", "05551000003", EmployeeTypes.JUNIOR, 30000.0);

        List<SearchCriteria> criteriaList = List.of(
                SearchCriteria.builder()
                        .field("employeeType")
                        .operator(SearchOperator.EQUAL)
                        .value("SENIOR")
                        .build(),
                SearchCriteria.builder()
                        .field("baseSalary")
                        .operator(SearchOperator.GREATER_THAN)
                        .value(45000.0)
                        .build()
        );

        mockMvc.perform(post("/api/employees/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(criteriaList)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Alice Senior")))
                .andExpect(jsonPath("$[0].employeeType", is("SENIOR")));
    }

    @Test
    void shouldSearchEmployeesByNameLike() throws Exception {
        createEmployee("Johnathan Smith", "john@company.com", "05552000001", EmployeeTypes.MANAGER, 90000.0);
        createEmployee("Jane Doe", "jane@company.com", "05552000002", EmployeeTypes.SENIOR, 50000.0);

        List<SearchCriteria> criteriaList = List.of(
                SearchCriteria.builder()
                        .field("name")
                        .operator(SearchOperator.LIKE)
                        .value("john")
                        .build()
        );

        mockMvc.perform(post("/api/employees/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(criteriaList)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Johnathan Smith")));
    }

    private void createEmployee(String name, String mail, String phone, EmployeeTypes type, double salary) throws Exception {
        EmployeeCreateRequestDto request = EmployeeCreateRequestDto.builder()
                .name(name)
                .mail(mail)
                .phoneNum(phone)
                .employeeType(type)
                .baseSalary(salary)
                .build();

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }
}
