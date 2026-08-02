package com.hrmanproject.domain.employee;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrmanproject.common.AbstractPostgresIntegrationTest;
import com.hrmanproject.domain.employee.dto.EmployeeCreateRequestDto;
import com.hrmanproject.domain.employee.dto.ProjectAssignmentRequestDto;
import com.hrmanproject.domain.employee.enums.EmployeeTypes;
import com.hrmanproject.domain.employee.repository.EmployeeRepository;
import com.hrmanproject.domain.project.dto.ProjectCreateRequestDto;
import com.hrmanproject.domain.project.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class EmployeeIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @BeforeEach
    void setUp() {
        employeeRepository.deleteAll();
        projectRepository.deleteAll();
    }

    @Test
    void shouldCreateEmployeeSuccessfully() throws Exception {
        EmployeeCreateRequestDto request = EmployeeCreateRequestDto.builder()
                .name("John Doe")
                .mail("john.doe@company.com")
                .phoneNum("05551112233")
                .employeeType(EmployeeTypes.SENIOR)
                .baseSalary(50000.0)
                .build();

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is("John Doe")))
                .andExpect(jsonPath("$.mail", is("john.doe@company.com")))
                .andExpect(jsonPath("$.employeeType", is("SENIOR")));
    }

    @Test
    void shouldFailWhenEmailOrPhoneIsInvalid() throws Exception {
        EmployeeCreateRequestDto invalidEmailRequest = EmployeeCreateRequestDto.builder()
                .name("Invalid Mail Employee")
                .mail("not-an-email")
                .phoneNum("05551112233")
                .employeeType(EmployeeTypes.JUNIOR)
                .baseSalary(30000.0)
                .build();

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidEmailRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldEnforceRoleHierarchyOnProjectAssignment() throws Exception {
        Long managerId = createEmployee("Manager User", "manager@company.com", "05551000001", EmployeeTypes.MANAGER, 80000.0);
        Long juniorId = createEmployee("Junior User", "junior@company.com", "05551000002", EmployeeTypes.JUNIOR, 25000.0);
        Long seniorId = createEmployee("Senior User", "senior@company.com", "05551000003", EmployeeTypes.SENIOR, 45000.0);
        Long projectId = createProject("Test Project", "Integration Test Project", 10);

        ProjectAssignmentRequestDto validAssignment = ProjectAssignmentRequestDto.builder()
                .assignerId(managerId)
                .targetEmployeeId(juniorId)
                .projectId(projectId)
                .build();

        mockMvc.perform(post("/api/employees/assign-project")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validAssignment)))
                .andExpect(status().isOk());

        ProjectAssignmentRequestDto invalidAssignment = ProjectAssignmentRequestDto.builder()
                .assignerId(juniorId)
                .targetEmployeeId(seniorId)
                .projectId(projectId)
                .build();

        mockMvc.perform(post("/api/employees/assign-project")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidAssignment)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldCompleteProjectAndRecalculateSalary() throws Exception {
        Long managerId = createEmployee("Manager User", "manager2@company.com", "05552000001", EmployeeTypes.MANAGER, 100000.0);
        Long juniorId = createEmployee("Junior User", "junior2@company.com", "05552000002", EmployeeTypes.JUNIOR, 30000.0);
        Long projectId = createProject("Salary Project", "Salary Recalculation Test", 10);

        ProjectAssignmentRequestDto assignment = ProjectAssignmentRequestDto.builder()
                .assignerId(managerId)
                .targetEmployeeId(juniorId)
                .projectId(projectId)
                .build();

        mockMvc.perform(post("/api/employees/assign-project")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignment)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/employees/" + juniorId + "/complete-project")
                        .param("projectId", projectId.toString())
                        .param("completionRate", "0.8")
                        .param("speedFactor", "2.0"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/employees/" + juniorId + "/manager-view")
                        .param("managerId", managerId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.baseSalary", is(30000.0)))
                .andExpect(jsonPath("$.performanceMultiplier", closeTo(1.16, 0.001)))
                .andExpect(jsonPath("$.currentSalary", closeTo(34800.0, 0.01)));
    }

    @Test
    void shouldEnforceManagerOnlySalaryView() throws Exception {
        Long managerId = createEmployee("Manager User", "manager3@company.com", "05553000001", EmployeeTypes.MANAGER, 90000.0);
        Long juniorId = createEmployee("Junior User", "junior3@company.com", "05553000002", EmployeeTypes.JUNIOR, 30000.0);
        Long seniorId = createEmployee("Senior User", "senior3@company.com", "05553000003", EmployeeTypes.SENIOR, 50000.0);

        mockMvc.perform(get("/api/employees/" + juniorId + "/manager-view")
                        .param("managerId", managerId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentSalary", is(30000.0)));

        mockMvc.perform(get("/api/employees/" + juniorId + "/manager-view")
                        .param("managerId", seniorId.toString()))
                .andExpect(status().isForbidden());
    }

    private Long createEmployee(String name, String mail, String phone, EmployeeTypes type, double salary) throws Exception {
        EmployeeCreateRequestDto request = EmployeeCreateRequestDto.builder()
                .name(name)
                .mail(mail)
                .phoneNum(phone)
                .employeeType(type)
                .baseSalary(salary)
                .build();

        String response = mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(response).get("id").asLong();
    }

    private Long createProject(String name, String desc, int days) throws Exception {
        ProjectCreateRequestDto request = ProjectCreateRequestDto.builder()
                .name(name)
                .description(desc)
                .targetDays(days)
                .build();

        String response = mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(response).get("id").asLong();
    }
}
