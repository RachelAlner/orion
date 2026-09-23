package com.orion.model;

import jakarta.persistence.*; 

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "tasks")
public class Task {

    @Id
    private UUID id;

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(name = "estimated_minutes", nullable = false)
    private Integer estimatedMinutes; 

    private LocalDateTime deadline; 

    @Column(nullable = false)
    private Integer priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status;

    @Column(nullable = false) 
    private OffsetDateTime createdAt;

    @Column(nullable = false)
    private OffsetDateTime updatedAt;

    private OffsetDateTime completedAt;

    protected Task() {}

    public Task(
            UUID projectId, 
            String title, 
            String description, 
            Integer estimatedMinutes, 
            LocalDateTime deadline, 
            Integer priority
    ) {
        this.id = UUID.randomUUID();
        this.projectId = projectId;
        this.title = title;
        this.description = description;
        this.estimatedMinutes = estimatedMinutes;
        this.deadline = deadline;
        this.priority = priority;
        this.status = TaskStatus.TODO;

        OffsetDateTime now = OffsetDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    public UUID getId() {
        return id;
    }

    public UUID getProjectId() {
        return projectId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Integer getEstimatedMinutes() {
        return estimatedMinutes;
    }

    public LocalDateTime getDeadline() {
        return deadline;
    }

    public Integer getPriority() {
        return priority;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public OffsetDateTime getCompletedAt() {
        return completedAt;
    }

    public void update(
            String title, 
            String description, 
            Integer estimatedMinutes,
            LocalDateTime deadline, 
            Integer priority
    ) {
        this.title = title;
        this.description = description;
        this.estimatedMinutes = estimatedMinutes;
        this.deadline = deadline;
        this.priority = priority;
        this.updatedAt = OffsetDateTime.now();
    }

    public void complete() {
        this.status = TaskStatus.COMPLETED;
        this.completedAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
        this.updatedAt = OffsetDateTime.now();
    }
}