package com.hrmanproject.domain.project;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrmanproject.common.AbstractPostgresIntegrationTest;
import com.hrmanproject.domain.project.dto.ProjectCompleteRequestDto;
import com.hrmanproject.domain.project.dto.ProjectCreateRequestDto;
import com.hrmanproject.domain.project.repository.ProjectRepository;
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

public class ProjectIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProjectRepository projectRepository;

    @BeforeEach
    void setUp() {
        projectRepository.deleteAll();
    }

    @Test
    void shouldCreateProjectSuccessfully() throws Exception {
        ProjectCreateRequestDto request = ProjectCreateRequestDto.builder()
                .name("AI Platform")
                .description("Next gen AI features")
                .targetDays(30)
                .build();

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is("AI Platform")))
                .andExpect(jsonPath("$.targetDays", is(30)))
                .andExpect(jsonPath("$.status", is("IN_PROGRESS")));
    }

    @Test
    void shouldCompleteProjectSuccessfullyAndPreventRecompletion() throws Exception {
        ProjectCreateRequestDto createRequest = ProjectCreateRequestDto.builder()
                .name("Cloud Migration")
                .description("AWS Migration")
                .targetDays(20)
                .build();

        String responseJson = mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long projectId = objectMapper.readTree(responseJson).get("id").asLong();

        ProjectCompleteRequestDto completeRequest = ProjectCompleteRequestDto.builder()
                .actualDays(10)
                .completionRate(1.0)
                .build();

        mockMvc.perform(post("/api/projects/" + projectId + "/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(completeRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("DONE")))
                .andExpect(jsonPath("$.actualDays", is(10)))
                .andExpect(jsonPath("$.completionRate", is(1.0)));

        mockMvc.perform(post("/api/projects/" + projectId + "/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(completeRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGetProjectById() throws Exception {
        ProjectCreateRequestDto createRequest = ProjectCreateRequestDto.builder()
                .name("Mobile App")
                .description("iOS and Android App")
                .targetDays(45)
                .build();

        String responseJson = mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long projectId = objectMapper.readTree(responseJson).get("id").asLong();

        mockMvc.perform(get("/api/projects/" + projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Mobile App")))
                .andExpect(jsonPath("$.targetDays", is(45)));
    }
}
