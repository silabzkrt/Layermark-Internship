package com.hrmanproject.domain.project.controller;

import com.hrmanproject.domain.project.dto.ProjectCompleteRequestDto;
import com.hrmanproject.domain.project.dto.ProjectCreateRequestDto;
import com.hrmanproject.domain.project.dto.ProjectResponseDto;
import com.hrmanproject.domain.project.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    public ResponseEntity<ProjectResponseDto> createProject(@Valid @RequestBody ProjectCreateRequestDto request) {
        ProjectResponseDto response = projectService.createProject(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponseDto> getProjectById(@PathVariable Long id) {
        ProjectResponseDto response = projectService.getProjectById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ProjectResponseDto>> getAllProjects() {
        List<ProjectResponseDto> response = projectService.getAllProjects();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<ProjectResponseDto> completeProject(@PathVariable Long id,
                                                              @Valid @RequestBody ProjectCompleteRequestDto request) {
        ProjectResponseDto response = projectService.completeProject(id, request);
        return ResponseEntity.ok(response);
    }
}
