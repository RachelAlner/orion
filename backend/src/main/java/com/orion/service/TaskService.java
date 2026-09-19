package com.orion.service;

import com.orion.service.ProjectService;
import com.orion.exception.TaskNotFoundException;
import com.orion.model.Task;
import com.orion.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service 
public class TaskService {
    private final ProjectService projectService;

    private final TaskRepository taskRepository;

    public TaskService(ProjectService projectService, TaskRepository taskRepository) {
        this.projectService = projectService;
        this.taskRepository = taskRepository;
    }

    public Task createTask(
             UUID userId, 
             UUID projectId, 
             String title, 
             String description, 
             Integer estimatedMinutes, 
             LocalDate deadline, 
             Integer priority
    ) {
        projectService.findByIdForUser(projectId, userId);

        Task task = new Task(
             projectId, 
             title, 
             description, 
             estimatedMinutes, 
             deadline, 
             priority
        );

        return taskRepository.save(task);
    }

    public List<Task> findAllForProject(UUID userId, UUID projectId) {
        projectService.findByIdForUser(projectId, userId);

        return taskRepository.findByProjectId(projectId);
    }

    public Task findByIdForProject(
            UUID userId,
            UUID taskId, 
            UUID projectId

    ) {
        projectService.findByIdForUser(projectId, userId);

        return taskRepository
                .findByIdAndProjectId(taskId, projectId)
                .orElseThrow(() -> 
                        new TaskNotFoundException(
                            "Task not found."
                        ));
    }

    public Task updateTask(
            UUID userId,
            UUID taskId, 
            UUID projectId, 
            String title, 
            String description, 
            Integer estimatedMinutes, 
            LocalDate deadline, 
            Integer priority
    ) {
        projectService.findByIdForUser(projectId, userId);

        Task task = findByIdForProject(userId, taskId, projectId);

        task.update(
                title, 
                description, 
                estimatedMinutes, 
                deadline, 
                priority
        );

        return taskRepository.save(task);
    }

    public Task completeTask(
            UUID userId, 
            UUID taskId, 
            UUID projectId 
    ) {
        projectService.findByIdForUser(projectId, userId);

        Task task = findByIdForProject(
                userId, 
                taskId, 
                projectId
        );

        task.complete();

        return taskRepository.save(task);
    }

    public void deleteTask(
            UUID userId, 
            UUID taskId, 
            UUID projectId
    ) {
        projectService.findByIdForUser(projectId, userId);

        Task task = findByIdForProject(
                userId, 
                taskId, 
                projectId
        );

        taskRepository.delete(task);
    }
}