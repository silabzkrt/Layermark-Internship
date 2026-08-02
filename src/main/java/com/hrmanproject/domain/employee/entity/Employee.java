package com.hrmanproject.domain.employee.entity;

import com.hrmanproject.domain.department.Department;
import com.hrmanproject.domain.employee.enums.EmployeeTypes;
import com.hrmanproject.domain.project.Project;
import com.hrmanproject.domain.project.enums.ProjectStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "employees")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Email(message = "Enter a valid mail")
    @Column(nullable = false, unique = true)
    private String mail;

    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Enter a valid phone number")
    @Column(nullable = false, unique = true)
    private String phoneNum;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Enumerated(EnumType.STRING)
    @Column(name = "employee_type", nullable = false)
    @NotNull(message = "Employee type is required")
    private EmployeeTypes employeeType = EmployeeTypes.NULL;

    @Column(name = "base_salary", nullable = false)
    private double baseSalary = 0.0;

    @Column(name = "performance_multiplier", nullable = false)
    private double performanceMultiplier = 1.0;

    @Column(name = "current_salary", nullable = false)
    private double currentSalary = 0.0;

    @ManyToMany
    @JoinTable(
            name = "employee_current_projects",
            joinColumns = @JoinColumn(name = "employee_id"),
            inverseJoinColumns = @JoinColumn(name = "project_id")
    )
    private List<Project> currentProjects = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "employee_completed_projects",
            joinColumns = @JoinColumn(name = "employee_id"),
            inverseJoinColumns = @JoinColumn(name = "project_id")
    )
    private List<Project> completedProjects = new ArrayList<>();

    protected Employee() {}

    public Employee(String name, String phoneNum, String mail, EmployeeTypes employeeType, double baseSalary) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty!");
        }
        if (employeeType == null || employeeType == EmployeeTypes.NULL) {
            throw new IllegalArgumentException("Valid EmployeeType is required!");
        }
        if (baseSalary < 0) {
            throw new IllegalArgumentException("Base salary cannot be negative!");
        }
        this.name = name;
        this.phoneNum = phoneNum;
        this.mail = mail;
        this.employeeType = employeeType;
        this.baseSalary = baseSalary;
        this.performanceMultiplier = 1.0;
        this.currentSalary = baseSalary;
    }

    public void recalculateSalary(double completionRate, double speedFactor) {
        if (completionRate < 0.0 || completionRate > 1.0) {
            throw new IllegalArgumentException("Completion rate must be between 0.0 and 1.0");
        }
        if (speedFactor < 0.0) {
            throw new IllegalArgumentException("Speed factor cannot be negative");
        }
        this.performanceMultiplier = (completionRate * 0.7) + (speedFactor * 0.3);
        this.currentSalary = this.baseSalary * this.performanceMultiplier;
    }

    public void assignProject(Project project) {
        if (project == null) {
            throw new IllegalArgumentException("Project cannot be null!");
        }
        if (!this.currentProjects.contains(project)) {
            this.currentProjects.add(project);
        }
    }

    public void finishProject(Project project) {
        if (project == null) {
            throw new IllegalArgumentException("Project cannot be null!");
        }
        if (project.getStatus() == ProjectStatus.DONE) {
            throw new IllegalStateException("Completed project cannot be redone!");
        }
        this.currentProjects.remove(project);
        if (!this.completedProjects.contains(project)) {
            this.completedProjects.add(project);
        }
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public EmployeeTypes getEmployeeType() {
        return employeeType;
    }

    public void setEmployeeType(EmployeeTypes employeeType) {
        this.employeeType = employeeType;
    }

    public double getBaseSalary() {
        return baseSalary;
    }

    public void setBaseSalary(double baseSalary) {
        this.baseSalary = baseSalary;
        this.currentSalary = this.baseSalary * this.performanceMultiplier;
    }

    public double getPerformanceMultiplier() {
        return performanceMultiplier;
    }

    public double getCurrentSalary() {
        return currentSalary;
    }

    public List<Project> getCurrentProjects() {
        return currentProjects;
    }

    public List<Project> getCompletedProjects() {
        return completedProjects;
    }
}
