package com.hrmanproject.domain.project.service;

import com.hrmanproject.domain.project.Project;
import com.hrmanproject.domain.project.dto.ProjectCompleteRequestDto;
import com.hrmanproject.domain.project.dto.ProjectCreateRequestDto;
import com.hrmanproject.domain.project.dto.ProjectResponseDto;
import com.hrmanproject.domain.project.repository.ProjectRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public ProjectResponseDto createProject(ProjectCreateRequestDto request) {
        if (projectRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Project name already exists: " + request.getName());
        }

        Project project = new Project(
                request.getName(),
                request.getDescription(),
                request.getTargetDays()
        );

        Project saved = projectRepository.save(project);
        return mapToResponseDto(saved);
    }

    @Transactional(readOnly = true)
    public ProjectResponseDto getProjectById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Project not found with id: " + id));
        return mapToResponseDto(project);
    }

    @Transactional(readOnly = true)
    public List<ProjectResponseDto> getAllProjects() {
        return projectRepository.findAll().stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public ProjectResponseDto completeProject(Long id, ProjectCompleteRequestDto request) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Project not found with id: " + id));

        project.completeProject(request.getActualDays(), request.getCompletionRate());
        Project saved = projectRepository.save(project);
        return mapToResponseDto(saved);
    }

    private ProjectResponseDto mapToResponseDto(Project project) {
        return ProjectResponseDto.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .status(project.getStatus())
                .targetDays(project.getTargetDays())
                .actualDays(project.getActualDays())
                .completionRate(project.getCompletionRate())
                .build();
    }
}
