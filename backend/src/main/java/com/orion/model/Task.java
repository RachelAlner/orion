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

    @Column(name = "estimated_minutes")
    private Integer estimatedMinutes; 

    @Column(name = "worked_minutes", nullable = false)
    private Integer workedMinutes;

    private LocalDateTime deadline; 

    @Column
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
        this.workedMinutes = 0;
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

    public Integer getWorkedMinutes() {
        return workedMinutes;
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

    public Integer getRemainingMinutes() {
        if (estimatedMinutes == null) {
            return null;
        }

        return Math.max(
                0,
                estimatedMinutes - workedMinutes 
        );
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

        if (estimatedMinutes == null) {
            this.status = TaskStatus.TODO;
            this.completedAt = null;
        } else if (getRemainingMinutes() == 0) {
            this.status = TaskStatus.COMPLETED;

            if (this.completedAt == null) {
                this.completedAt = OffsetDateTime.now();
            }
        } else {
            this.status = TaskStatus.IN_PROGRESS;
            this.completedAt = null;
        }

        this.updatedAt = OffsetDateTime.now();
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
        this.updatedAt = OffsetDateTime.now();
    }

    public void updateProgress(int workedMinutes) {
        if (workedMinutes < 0) {
            throw new IllegalArgumentException(
                    "Minutes worked cannot be negative"
            );
        }

        if (estimatedMinutes == null) {
            throw new IllegalStateException(
                "Cannot record progress for a task without an estimated duration"
            );
        }

        this.workedMinutes = workedMinutes;

        if (getRemainingMinutes() == 0) {
            this.status = TaskStatus.COMPLETED;

            if (this.completedAt == null) {
                this.completedAt = OffsetDateTime.now();
            }
        } else if (workedMinutes == 0) { 
            this.status = TaskStatus.TODO;
            this.completedAt = null;
        } else {
            this.status = TaskStatus.IN_PROGRESS;
            this.completedAt = null;
        }

        this.updatedAt = OffsetDateTime.now();
    }

    public void complete() {
        if (estimatedMinutes != null) {
            this.workedMinutes = estimatedMinutes;
        }

        this.status = TaskStatus.COMPLETED;
        this.completedAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
    }
}