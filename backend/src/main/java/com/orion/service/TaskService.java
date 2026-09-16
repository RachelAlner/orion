package com.orion.service;

import com.orion.exception.TaskNotFoundException;
import com.orion.model.Task;
import com.orion.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service 
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public Task createTask(
            UUID projectId, 
            String title, 
            String description, 
            Integer estimatedMinutes, 
            LocalDate deadline, 
            Integer priority
    ) {
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

    public List<Task> findAllForProject(UUID projectId) {
        return taskRepository.findByProjectId(projectId);
    }

    public Task findByIdForProject(
            UUID taskId, 
            UUID projectId
    ) {
        return taskRepository
                .findByIdAndProjectId(taskId, projectId)
                .orElseThrow(() -> 
                        new TaskNotFoundException(
                            "Task not found."
                        ));
    }

    public Task updateTask(
            UUID taskId, 
            UUID projectId, 
            String title, 
            String description, 
            Integer estimatedMinutes, 
            LocalDate deadline, 
            Integer priority
    ) {
        Task task = findByIdForProject(taskId, projectId);

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
            UUID taskId, 
            UUID projectId 
    ) {
        Task task = findByIdForProject(
                taskId, 
                projectId
        );

        task.complete();

        return taskRepository.save(task);
    }

    public void deleteTask(
            UUID taskId, 
            UUID projectId
    ) {
        Task task = findByIdForProject(
                taskId, 
                projectId
        );

        taskRepository.delete(task);
    }
}