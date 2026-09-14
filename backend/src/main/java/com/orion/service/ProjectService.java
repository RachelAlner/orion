package com.orion.service;

import com.orion.exception.ProjectNotFoundException;
import com.orion.model.Project;
import com.orion.repository.ProjectRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service 
public class ProjectService {
    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public List<Project> findAllForUser(UUID userId) {
        return projectRepository.findByUserId(userId);
    }

    public Project findByIdForUser(UUID projectId, UUID userId) {
        return projectRepository
            .findByIdAndUserId(projectId, userId)
            .orElseThrow(() -> 
                new ProjectNotFoundException("Project not found."));
    }

    public Project createProject(
            UUID userId, 
            String name, 
            String description, 
            LocalDate deadline, 
            Integer priority
    ) {
        Project project = new Project(
                userId, 
                name, 
                description, 
                deadline, 
                priority
        );

        return projectRepository.save(project);
    }

    public Project updateProject(
            UUID projectId,
            UUID userId, 
            String name, 
            String description, 
            LocalDate deadline, 
            Integer priority
    ) {
        Project project = findByIdForUser(projectId, userId);

        project.update(
                name, 
                description, 
                deadline, 
                priority
        );

        return projectRepository.save(project);
    }

    public void deleteProject(UUID projectId, UUID userId) {
        Project project = findByIdForUser(projectId, userId);

        projectRepository.delete(project);
    }
}