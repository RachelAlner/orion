package com.orion.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "projects")
public class Project {

    @Id 
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String name;

    private String description;

    private LocalDate deadline;

    @Column(nullable = false)
    private Integer priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectStatus status;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    @Column(nullable = false)
    private OffsetDateTime updatedAt;

    protected Project() {}

    public Project(
        UUID userId, 
        String name,
        String description, 
        LocalDate deadline, 
        Integer priority
    ) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.name = name;
        this.description = description;
        this.deadline = deadline;
        this.priority = priority;
        this.status = ProjectStatus.ACTIVE;

        OffsetDateTime now = OffsetDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public Integer getPriority() {
        return priority;
    }

    public ProjectStatus getStatus() {
        return status;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void update(
            String name, 
            String description, 
            LocalDate deadline, 
            Integer priority
    ) {
        this.name = name;
        this.description = description;
        this.deadline = deadline;
        this.priority = priority;
        this.updatedAt = OffsetDateTime.now();
    }

    public void setStatus(ProjectStatus status) {
        this.status = status;
        this.updatedAt = OffsetDateTime.now();
    }

}