package com.orion.controller;

import com.orion.dto.CreateTaskRequest;
import com.orion.dto.TaskResponse;
import com.orion.dto.UpdateTaskRequest;
import com.orion.model.Task;
import com.orion.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/projects/{projectId}/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public ResponseEntity<List<TaskResponse>> getTasks(
            @PathVariable UUID projectId, 
            Authentication authentication
    ) {
        UUID userId = getUserId(authentication);

        List<TaskResponse> tasks = 
                taskService.findAllForProject(userId, projectId)
                        .stream()
                        .map(this::toResponse)
                        .toList();

        return ResponseEntity.ok(tasks);     
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<TaskResponse> getTask(
            @PathVariable UUID projectId, 
            @PathVariable UUID taskId, 
            Authentication authentication
    ) {
        UUID userId = getUserId(authentication);

        Task task = 
                taskService.findByIdForProject(
                        userId, 
                        taskId, 
                        projectId
                );

        return ResponseEntity.ok(toResponse(task));
    }

    @PostMapping 
    public ResponseEntity<TaskResponse> createTask(
            @PathVariable UUID projectId, 
            @Valid @RequestBody CreateTaskRequest request, 
            Authentication authentication
    ) {
        UUID userId = getUserId(authentication);

        Task task = 
                taskService.createTask(
                        userId, 
                        projectId, 
                        request.title(), 
                        request.description(), 
                        request.estimatedMinutes(), 
                        request.deadline(), 
                        request.priority()
                );
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(toResponse(task));
    }

    @PutMapping("/{taskId}")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable UUID projectId, 
            @PathVariable UUID taskId, 
            @Valid @RequestBody UpdateTaskRequest request, 
            Authentication authentication
    ) {
        UUID userId = getUserId(authentication);

        Task task = 
                taskService.updateTask(
                        userId, 
                        taskId, 
                        projectId, 
                        request.title(), 
                        request.description(), 
                        request.estimatedMinutes(), 
                        request.deadline(), 
                        request.priority()
                );
        
        return ResponseEntity.ok(toResponse(task));
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable UUID projectId, 
            @PathVariable UUID taskId, 
            Authentication authentication
    ) {
        UUID userId = getUserId(authentication);

        taskService.deleteTask(
                userId, 
                taskId, 
                projectId
        );

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{taskId}/complete")
    public ResponseEntity<TaskResponse> completeTask(
            @PathVariable UUID projectId, 
            @PathVariable UUID taskId, 
            Authentication authentication
    ) {
        UUID userId = getUserId(authentication);

        Task task = 
                taskService.completeTask(
                        userId, 
                        taskId, 
                        projectId
                );
        
        return ResponseEntity.ok(toResponse(task));
    }

    private TaskResponse toResponse(Task task) {
        return new TaskResponse(
                task.getId(), 
                task.getProjectId(), 
                task.getTitle(), 
                task.getDescription(), 
                task.getEstimatedMinutes(), 
                task.getDeadline(), 
                task.getPriority(), 
                task.getStatus(), 
                task.getCreatedAt(), 
                task.getUpdatedAt(), 
                task.getCompletedAt()
        );
    }

    private UUID getUserId(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }
}