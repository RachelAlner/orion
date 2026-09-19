package com.orion.service;

import com.orion.exception.DependencyCycleException;
import com.orion.exception.InvalidTaskDependencyException;
import com.orion.exception.TaskDependencyAlreadyExistsException;
import com.orion.exception.TaskDependencyNotFoundException;
import com.orion.model.Task;
import com.orion.model.TaskDependency;
import com.orion.model.TaskDependencyId;
import com.orion.repository.TaskRepository;
import com.orion.repository.TaskDependencyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service 
public class TaskDependencyService {

    private final TaskDependencyRepository taskDependencyRepository;
    private final TaskRepository taskRepository;
    private final ProjectService projectService;

    public TaskDependencyService(
            TaskDependencyRepository taskDependencyRepository,
            TaskRepository taskRepository,
            ProjectService projectService
    ) {
        this.taskDependencyRepository = taskDependencyRepository;
        this.taskRepository = taskRepository;
        this.projectService = projectService;
    }

    @Transactional
    public TaskDependency addDependency(
            UUID userId, 
            UUID projectId, 
            UUID taskId, 
            UUID dependsOnTaskId
    ) {
        // verify the project belongs to the authenticated user. 
        projectService.findByIdForUser(projectId, userId);

        // verify the task belongs to the project.
        Task task = taskRepository
                .findByIdAndProjectId(taskId, projectId)
                .orElseThrow(() -> 
                        new InvalidTaskDependencyException(
                                "Task does not belong to the specified project."
                        ));

        // find the prerequisite task 
        Task prerequisite = taskRepository
                .findById(dependsOnTaskId) 
                .orElseThrow(() ->
                        new InvalidTaskDependencyException(
                                "Dependency task not found."
                        ));
        
        // verify the prerequisite task belongs to a project
        // owned by the same authenticated user.
        projectService.findByIdForUser(
                prerequisite.getProjectId(), 
                userId
        );

        // task cannot depend on itself. 
        if (task.getId().equals(prerequisite.getId())) {
            throw new InvalidTaskDependencyException(
                    "A task cannot depend on itself."
            );
        }
        
        // prevent duplicate dependencies. 
        if (taskDependencyRepository
                .existsByIdTaskIdAndIdDependsOnTaskId(
                        taskId, 
                        dependsOnTaskId
                )) {
            throw new TaskDependencyAlreadyExistsException(
                    "Task dependency already exists."
            );
        }

        // adding taskId -> dependsOnTaskId 
        // creates a cycle 
        // if taskId is already reachable from dependsOnTaskId.
        if(wouldCreateCycle(taskId, dependsOnTaskId)) {
            throw new DependencyCycleException(
                    "Adding this dependency would create a cycle."
            );
        }

        TaskDependency dependency = 
                new TaskDependency(
                        new TaskDependencyId(
                                taskId, 
                                dependsOnTaskId
                        )
                );
        
        return taskDependencyRepository.save(dependency);
    }

    @Transactional(readOnly = true)
    public List<TaskDependency> findDependencies(
            UUID userId, 
            UUID projectId, 
            UUID taskId
    ) {
        projectService.findByIdForUser(projectId, userId);

        taskRepository
                .findByIdAndProjectId(taskId, projectId)
                .orElseThrow(() -> 
                        new InvalidTaskDependencyException(
                            "Task does not belong to the specified project"
                        ));
        return taskDependencyRepository.findByIdTaskId(taskId);
    }

    @Transactional
    public void removeDependency(
            UUID userId, 
            UUID projectId, 
            UUID taskId, 
            UUID dependsOnTaskId
    ) {
        projectService.findByIdForUser(projectId, userId);

        taskRepository
                .findByIdAndProjectId(taskId, projectId)
                .orElseThrow(() -> 
                        new InvalidTaskDependencyException(
                                "Task does not belong to the specified project."
                        ));
        
        Task prerequisite = taskRepository
                .findById(dependsOnTaskId)
                .orElseThrow(() -> 
                        new TaskDependencyNotFoundException(
                                "Task dependency not found."
                        )
                );

        // ensure prerequisite task belongs to the same user.
        projectService.findByIdForUser(
                prerequisite.getProjectId(), 
                userId
        );

        TaskDependencyId dependencyId = 
                new TaskDependencyId(
                        taskId, 
                        dependsOnTaskId
                );
        
        TaskDependency dependency = 
                taskDependencyRepository
                        .findById(dependencyId)
                        .orElseThrow(() -> 
                                new TaskDependencyNotFoundException(
                                        "Task dependency not found."
                                )
                        );

        taskDependencyRepository.delete(dependency);
    }

    private boolean wouldCreateCycle(
            UUID taskId, 
            UUID dependsOnTaskId
    ) {
        Set<UUID> visited = new HashSet<>();

        return canReach(
                dependsOnTaskId, 
                taskId, 
                visited
        );
    }

    private boolean canReach(
            UUID currentTaskId, 
            UUID targetTaskId, 
            Set<UUID> visited
    ) {
        if(currentTaskId.equals(targetTaskId)) {
            return true;
        }

        if(!visited.add(currentTaskId)) {
            return false;
        }

        List<TaskDependency> dependencies = 
                taskDependencyRepository
                        .findByIdTaskId(currentTaskId);
        
        for (TaskDependency dependency : dependencies) {

            UUID prerequisiteId = dependency.getId().getDependsOnTaskId();

            if (canReach(
                    prerequisiteId, 
                    targetTaskId, 
                    visited
            )) {
                return true;
            }
        }

        return false;
    }
}