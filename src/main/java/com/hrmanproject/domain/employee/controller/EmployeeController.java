package com.hrmanproject.domain.employee.controller;

import com.hrmanproject.domain.employee.dto.EmployeeCreateRequestDto;
import com.hrmanproject.domain.employee.dto.EmployeeResponseDto;
import com.hrmanproject.domain.employee.dto.ManagerEmployeeViewDto;
import com.hrmanproject.domain.employee.dto.ProjectAssignmentRequestDto;
import com.hrmanproject.common.search.SearchCriteria;
import com.hrmanproject.domain.employee.service.EmployeeService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping
    public ResponseEntity<EmployeeResponseDto> createEmployee(@Valid @RequestBody EmployeeCreateRequestDto request) {
        EmployeeResponseDto response = employeeService.createEmployee(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponseDto> getEmployeeById(@PathVariable Long id) {
        EmployeeResponseDto response = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/manager-view")
    public ResponseEntity<ManagerEmployeeViewDto> getEmployeeForManager(@RequestParam Long managerId,
                                                                        @PathVariable("id") Long targetEmployeeId) {
        ManagerEmployeeViewDto response = employeeService.getEmployeeForManager(managerId, targetEmployeeId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/assign-project")
    public ResponseEntity<Object> assignProject(@Valid @RequestBody ProjectAssignmentRequestDto request) {
        Object response = employeeService.assignProject(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/complete-project")
    public ResponseEntity<Void> completeProject(@PathVariable("id") Long employeeId,
                                                @RequestParam Long projectId,
                                                @RequestParam double completionRate,
                                                @RequestParam double speedFactor) {
        employeeService.completeProject(employeeId, projectId, completionRate, speedFactor);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/search")
    public ResponseEntity<List<EmployeeResponseDto>> searchEmployees(@RequestBody List<SearchCriteria> criteriaList) {
        List<EmployeeResponseDto> response = employeeService.searchEmployees(criteriaList);
        return ResponseEntity.ok(response);
    }
}
