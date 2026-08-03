package com.hrmanproject.domain.employee.service;

import com.hrmanproject.domain.department.Department;
import com.hrmanproject.domain.department.repository.DepartmentRepository;
import com.hrmanproject.domain.employee.dto.EmployeeCreateRequestDto;
import com.hrmanproject.domain.employee.dto.EmployeeResponseDto;
import com.hrmanproject.domain.employee.dto.ManagerEmployeeViewDto;
import com.hrmanproject.domain.employee.dto.ProjectAssignmentRequestDto;
import com.hrmanproject.domain.employee.entity.Employee;
import com.hrmanproject.domain.employee.enums.EmployeeTypes;
import com.hrmanproject.domain.employee.repository.EmployeeRepository;
import com.hrmanproject.domain.project.Project;
import com.hrmanproject.domain.project.repository.ProjectRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.jpa.domain.Specification;
import com.hrmanproject.common.search.SearchCriteria;
import com.hrmanproject.common.search.SpecificationBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final ProjectRepository projectRepository;

    public EmployeeService(EmployeeRepository employeeRepository,
                           DepartmentRepository departmentRepository,
                           ProjectRepository projectRepository) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.projectRepository = projectRepository;
    }

    public EmployeeResponseDto createEmployee(EmployeeCreateRequestDto request) {
        if (employeeRepository.existsByMail(request.getMail())) {
            throw new IllegalArgumentException("Email already exists: " + request.getMail());
        }
        if (employeeRepository.existsByPhoneNum(request.getPhoneNum())) {
            throw new IllegalArgumentException("Phone number already exists: " + request.getPhoneNum());
        }

        EmployeeTypes type = request.getEmployeeType();
        if (type == null || type == EmployeeTypes.NULL) {
            throw new IllegalArgumentException("A valid EmployeeTypes value is required");
        }

        Employee employee = new Employee(
                request.getName(),
                request.getPhoneNum(),
                request.getMail(),
                type,
                request.getBaseSalary()
        );

        if (request.getDepartmentId() != null) {
            Department department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new EntityNotFoundException("Department not found with id: " + request.getDepartmentId()));
            employee.setDepartment(department);
        }

        Employee saved = employeeRepository.save(employee);
        return mapToPublicResponseDto(saved);
    }

    @Transactional(readOnly = true)
    public EmployeeResponseDto getEmployeeById(Long id) {
        Employee employee = findEmployeeOrThrow(id);
        return mapToPublicResponseDto(employee);
    }

    @Transactional(readOnly = true)
    public ManagerEmployeeViewDto getEmployeeForManager(Long managerId, Long targetEmployeeId) {
        Employee manager = findEmployeeOrThrow(managerId);
        if (!manager.getEmployeeType().isManagerial()) {
            throw new SecurityException("Only managers can view salary and multiplier details.");
        }
        Employee target = findEmployeeOrThrow(targetEmployeeId);
        return mapToManagerViewDto(target);
    }

    public Object assignProject(ProjectAssignmentRequestDto request) {
        Employee assigner = findEmployeeOrThrow(request.getAssignerId());
        Employee target = findEmployeeOrThrow(request.getTargetEmployeeId());
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new EntityNotFoundException("Project not found with id: " + request.getProjectId()));

        EmployeeTypes assignerType = assigner.getEmployeeType();
        EmployeeTypes targetType = target.getEmployeeType();

        if (!assignerType.canAssignProjectTo(targetType)) {
            throw new SecurityException("Role " + assignerType
                    + " is not authorized to assign projects to role " + targetType);
        }

        target.assignProject(project);
        Employee saved = employeeRepository.save(target);

        if (assignerType.isManagerial()) {
            return mapToManagerViewDto(saved);
        }
        return mapToPublicResponseDto(saved);
    }

    public void completeProject(Long employeeId, Long projectId, double completionRate, double speedFactor) {
        Employee employee = findEmployeeOrThrow(employeeId);
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found with id: " + projectId));
        employee.finishProject(project);
        employee.recalculateSalary(completionRate, speedFactor);
        employeeRepository.save(employee);
    }

    private Employee findEmployeeOrThrow(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found with id: " + id));
    }

    private EmployeeResponseDto mapToPublicResponseDto(Employee employee) {
        return EmployeeResponseDto.builder()
                .id(employee.getId())
                .name(employee.getName())
                .mail(employee.getMail())
                .phoneNum(employee.getPhoneNum())
                .employeeType(employee.getEmployeeType())
                .departmentId(employee.getDepartment() != null ? employee.getDepartment().getId() : null)
                .currentProjectIds(mapProjectIds(employee.getCurrentProjects()))
                .completedProjectIds(mapProjectIds(employee.getCompletedProjects()))
                .build();
    }

    private ManagerEmployeeViewDto mapToManagerViewDto(Employee employee) {
        return ManagerEmployeeViewDto.builder()
                .id(employee.getId())
                .name(employee.getName())
                .mail(employee.getMail())
                .phoneNum(employee.getPhoneNum())
                .employeeType(employee.getEmployeeType())
                .departmentId(employee.getDepartment() != null ? employee.getDepartment().getId() : null)
                .baseSalary(employee.getBaseSalary())
                .performanceMultiplier(employee.getPerformanceMultiplier())
                .currentSalary(employee.getCurrentSalary())
                .currentProjectIds(mapProjectIds(employee.getCurrentProjects()))
                .completedProjectIds(mapProjectIds(employee.getCompletedProjects()))
                .build();
    }

    @Transactional(readOnly = true)
    public List<EmployeeResponseDto> searchEmployees(List<SearchCriteria> criteriaList) {
        Specification<Employee> specification = SpecificationBuilder.build(criteriaList);
        return employeeRepository.findAll(specification).stream()
                .map(this::mapToPublicResponseDto)
                .collect(Collectors.toList());
    }

    private List<Long> mapProjectIds(List<Project> projects) {
        if (projects == null) return List.of();
        return projects.stream()
                .map(Project::getId)
                .collect(Collectors.toList());
    }
}
