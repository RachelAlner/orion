package com.orion.controller;

import com.orion.dto.CreateProjectRequest;
import com.orion.dto.ProjectResponse;
import com.orion.dto.UpdateProjectRequest;
import com.orion.model.Project;
import com.orion.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController 
@RequestMapping("/api/projects")
public class ProjectController {
    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping 
    public ResponseEntity<List<ProjectResponse>> getProjects(
            Authentication authentication
    ) {
        UUID userId = getUserId(authentication);

        List<ProjectResponse> projects = 
                projectService.findAllForUser(userId)
                        .stream()
                        .map(this::toResponse)
                        .toList();

        return ResponseEntity.ok(projects);
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectResponse> getProject(
            @PathVariable UUID projectId, 
            Authentication authentication
    ) {
        UUID userId = getUserId(authentication);

        Project project = projectService.findByIdForUser(projectId, userId);

        return ResponseEntity.ok(toResponse(project));
    }

    @PostMapping 
    public ResponseEntity<ProjectResponse> createProject(
            @Valid @RequestBody CreateProjectRequest request, 
            Authentication authentication
    ) {
        UUID userId = getUserId(authentication);

        Project project = 
                projectService.createProject(
                        userId, 
                        request.name(), 
                        request.description(), 
                        request.deadline(), 
                        request.priority()
        );

        URI location = URI.create(
                "/api/projects/" + project.getId()
        );

        return ResponseEntity
                .created(location)
                .body(toResponse(project));
    }

    @PutMapping("/{projectId}")
    public ResponseEntity<ProjectResponse> updateProject(
            @PathVariable UUID projectId, 
            @Valid @RequestBody UpdateProjectRequest request, 
            Authentication authentication
    ) {
        UUID userId = getUserId(authentication);

        Project project = 
                projectService.updateProject(
                        projectId, 
                        userId, 
                        request.name(), 
                        request.description(), 
                        request.deadline(), 
                        request.priority()
                );

        return ResponseEntity.ok(toResponse(project));

    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<Void> deleteProject(
            @PathVariable UUID projectId, 
            Authentication authentication
    ) {
        UUID userId = getUserId(authentication);

        projectService.deleteProject(
            projectId, 
            userId
        );     

        return ResponseEntity.noContent().build();

    }

    private UUID getUserId(Authentication authentication) {
        return UUID.fromString(
                authentication.getName()
        );
    }

    private ProjectResponse toResponse(Project project) {
        return new ProjectResponse(
                project.getId(), 
                project.getName(), 
                project.getDescription(), 
                project.getDeadline(), 
                project.getPriority(), 
                project.getStatus(), 
                project.getCreatedAt(), 
                project.getUpdatedAt()
        );
    }
}