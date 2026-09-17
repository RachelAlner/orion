package com.orion.model;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "task_dependencies")
public class TaskDependency {

    @EmbeddedId
    private TaskDependencyId id;

    protected TaskDependency() {

    }

    public TaskDependency(
            TaskDependencyId id
    ) {
        this.id = id;
    }

    public TaskDependencyId getId() {
        return id;
    }
}