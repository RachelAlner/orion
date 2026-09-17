package com.orion.repository;

import com.orion.model.TaskDependency;
import com.orion.model.TaskDependencyId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TaskDependencyRepository extends JpaRepository<TaskDependency, TaskDependencyId> {

    List<TaskDependency> findByIdTaskId(UUID taskId);

    List<TaskDependency> findByIdDependsOnTaskId(
            UUID dependsOnTaskId
    );

    boolean existsByIdTaskIdAndIdDependsOnTaskId(
            UUID taskId, 
            UUID dependsOnTaskId
    );
}