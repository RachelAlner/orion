package com.orion.service;

import com.orion.model.Project;
import com.orion.repository.ProjectRepository;
import org.springframework.stereotype.Service;

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
                new IllegalArgumentException("Project not found."));
    }

    public Project save(Project project) {
        return projectRepository.save(project);
    }

    public void delete(Project project) {
        projectRepository.delete(project);
    }
}