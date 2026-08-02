package com.hrmanproject.domain.department;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrmanproject.common.AbstractPostgresIntegrationTest;
import com.hrmanproject.domain.department.dto.DepartmentCreateRequestDto;
import com.hrmanproject.domain.department.repository.DepartmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class DepartmentIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DepartmentRepository departmentRepository;

    @BeforeEach
    void setUp() {
        departmentRepository.deleteAll();
    }

    @Test
    void shouldCreateDepartmentSuccessfully() throws Exception {
        DepartmentCreateRequestDto request = DepartmentCreateRequestDto.builder()
                .name("Engineering")
                .code("ENG-01")
                .build();

        mockMvc.perform(post("/api/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is("Engineering")))
                .andExpect(jsonPath("$.code", is("ENG-01")));
    }

    @Test
    void shouldFailWhenDepartmentCodeAlreadyExists() throws Exception {
        DepartmentCreateRequestDto firstRequest = DepartmentCreateRequestDto.builder()
                .name("Human Resources")
                .code("HR-01")
                .build();

        mockMvc.perform(post("/api/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstRequest)))
                .andExpect(status().isCreated());

        DepartmentCreateRequestDto duplicateCodeRequest = DepartmentCreateRequestDto.builder()
                .name("HR Team Two")
                .code("HR-01")
                .build();

        mockMvc.perform(post("/api/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateCodeRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGetDepartmentByIdAndGetAll() throws Exception {
        DepartmentCreateRequestDto request = DepartmentCreateRequestDto.builder()
                .name("Finance")
                .code("FIN-01")
                .build();

        String responseJson = mockMvc.perform(post("/api/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long departmentId = objectMapper.readTree(responseJson).get("id").asLong();

        mockMvc.perform(get("/api/departments/" + departmentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Finance")))
                .andExpect(jsonPath("$.code", is("FIN-01")));

        mockMvc.perform(get("/api/departments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)));
    }
}
