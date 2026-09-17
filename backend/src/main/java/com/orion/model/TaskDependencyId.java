package com.orion.model;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
public class TaskDependencyId implements Serializable {

    private UUID taskId;
    private UUID dependsOnTaskId;

    protected TaskDependencyId() {

    }

    public TaskDependencyId(
            UUID taskId, 
            UUID dependsOnTaskId
    ) {
        this.taskId = taskId;
        this.dependsOnTaskId = dependsOnTaskId;
    }

    public UUID getTaskId() {
        return taskId;
    }

    public UUID getDependsOnTaskId() {
        return dependsOnTaskId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof TaskDependencyId other)) {
            return false;
        }

        return taskId.equals(other.taskId)
                && dependsOnTaskId.equals(other.dependsOnTaskId);
    }

    @Override 
    public int hashCode() {
        return 31 * taskId.hashCode()
                + dependsOnTaskId.hashCode();
    }
}