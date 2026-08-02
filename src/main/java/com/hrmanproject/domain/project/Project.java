package com.hrmanproject.domain.project;

import com.hrmanproject.domain.project.enums.ProjectStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "projects")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectStatus status = ProjectStatus.IN_PROGRESS;

    @Column(name = "target_days", nullable = false)
    private int targetDays;

    @Column(name = "actual_days")
    private int actualDays;

    @Column(name = "completion_rate")
    private double completionRate;

    protected Project() {}

    public Project(String name, String description, int targetDays) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Project name cannot be empty!");
        }
        if (targetDays <= 0) {
            throw new IllegalArgumentException("Target days must be greater than zero!");
        }
        this.name = name;
        this.description = description;
        this.targetDays = targetDays;
        this.status = ProjectStatus.IN_PROGRESS;
        this.actualDays = 0;
        this.completionRate = 0.0;
    }

    public void completeProject(int actualDays, double completionRate) {
        if (this.status == ProjectStatus.DONE) {
            throw new IllegalStateException("Project is already completed!");
        }
        if (actualDays <= 0) {
            throw new IllegalArgumentException("Actual days must be greater than zero!");
        }
        if (completionRate < 0.0 || completionRate > 1.0) {
            throw new IllegalArgumentException("Completion rate must be between 0.0 and 1.0");
        }
        this.actualDays = actualDays;
        this.completionRate = completionRate;
        this.status = ProjectStatus.DONE;
    }

    public double calculateSpeedFactor() {
        if (this.targetDays <= 0 || this.actualDays <= 0) {
            return 1.0;
        }
        return (double) this.targetDays / (double) this.actualDays;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ProjectStatus getStatus() {
        return status;
    }

    public void setStatus(ProjectStatus status) {
        this.status = status;
    }

    public int getTargetDays() {
        return targetDays;
    }

    public void setTargetDays(int targetDays) {
        this.targetDays = targetDays;
    }

    public int getActualDays() {
        return actualDays;
    }

    public double getCompletionRate() {
        return completionRate;
    }
}
