package com.orion.controller;

import com.orion.dto.AddTaskDependencyRequest;
import com.orion.dto.TaskDependencyResponse;
import com.orion.model.TaskDependency;
import com.orion.service.TaskDependencyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController 
@RequestMapping("/api/projects/{projectId}/tasks/{taskId}/dependencies")
public class TaskDependencyController {

    private final TaskDependencyService taskDependencyService;

    public TaskDependencyController(
            TaskDependencyService taskDependencyService
    ) {
        this.taskDependencyService = taskDependencyService;
    }

    @PostMapping 
    public ResponseEntity<TaskDependencyResponse> addDependency(
            @PathVariable UUID projectId, 
            @PathVariable UUID taskId, 
            @Valid @RequestBody AddTaskDependencyRequest request, 
            Authentication authentication 
    ) {
        UUID userId = getUserId(authentication);

        TaskDependency dependency = 
                taskDependencyService.addDependency(
                        userId, 
                        projectId, 
                        taskId, 
                        request.dependsOnTaskId()
                );
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(toResponse(dependency));
    }

    @GetMapping 
    public ResponseEntity<List<TaskDependencyResponse>> getDependencies(
            @PathVariable UUID projectId, 
            @PathVariable UUID taskId, 
            Authentication authentication
    ) {
        UUID userId = getUserId(authentication);
        
        List<TaskDependencyResponse> dependencies = 
                taskDependencyService
                        .findDependencies(
                                userId, 
                                projectId, 
                                taskId
                        )
                        .stream()
                        .map(this::toResponse)
                        .toList();
        
        return ResponseEntity.ok(dependencies);
    }

    @DeleteMapping("/{dependsOnTaskId}")
    public ResponseEntity<Void> removeDependency(
            @PathVariable UUID projectId, 
            @PathVariable UUID taskId, 
            @PathVariable UUID dependsOnTaskId, 
            Authentication authentication
    ) {
        UUID userId = getUserId(authentication);
        
        taskDependencyService.removeDependency(
                userId, 
                projectId, 
                taskId, 
                dependsOnTaskId
        );

        return ResponseEntity.noContent().build();
    }

    private TaskDependencyResponse toResponse(
            TaskDependency dependency
    ) {
        return new TaskDependencyResponse(
                dependency.getId().getTaskId(), 
                dependency.getId().getDependsOnTaskId()
        );
    }

    private UUID getUserId(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }
}