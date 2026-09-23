package com.orion.service; 

import com.orion.model.Project;
import com.orion.repository.ProjectRepository;
import com.orion.exception.ProjectNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {
    @Mock 
    private ProjectRepository projectRepository;

    private ProjectService projectService;

    @BeforeEach
    void setUp() {
        projectService = new ProjectService(projectRepository);
    }

    @Test 
    void shouldFindAllProjectsForUser() {
        UUID userId = UUID.randomUUID();

        Project firstProject = new Project(
                userId, 
                "Orion", 
                "Scheduling app", 
                LocalDateTime.of(2027, 6, 1, 0, 0), 
                5
        );

        Project secondProject = new Project(
                userId, 
                "Database", 
                "Database project", 
                LocalDateTime.of(2027, 5, 20, 0, 0), 
                4
        ); 

        when(projectRepository.findByUserId(userId)).thenReturn(List.of(firstProject, secondProject));

        List<Project> result = projectService.findAllForUser(userId);

        assertEquals(2, result.size());
        assertEquals(firstProject, result.get(0));
        assertEquals(secondProject, result.get(1));
        
        verify(projectRepository).findByUserId(userId);
    }

    @Test 
    void shouldReturnEmptyListWhenUserHasNoProjects() {
        UUID userId = UUID.randomUUID();

        when(projectRepository.findByUserId(userId)).thenReturn(List.of());

        List<Project> result = projectService.findAllForUser(userId);

        assertTrue(result.isEmpty());

        verify(projectRepository).findByUserId(userId);
    }

    @Test 
    void shouldFindProjectForUser() {
        UUID userId = UUID.randomUUID();

        Project project = new Project(
                userId, 
                "Orion", 
                "Scheduling app", 
                LocalDateTime.of(2027, 6, 1, 0, 0), 
                5
        );

        when(projectRepository.findByIdAndUserId(project.getId(), userId)).thenReturn(Optional.of(project));

        Project result = projectService.findByIdForUser(project.getId(), userId);

        assertNotNull(result);
        assertEquals(project, result);

        verify(projectRepository).findByIdAndUserId(project.getId(), userId);
    }

    @Test 
    void shouldRejectProjectOwnedByAnotherUser() {
        UUID ownerId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();

        Project project = new Project(
            ownerId, 
            "Orion", 
            "Scheduling app", 
            LocalDateTime.of(2027, 6, 2, 0, 0), 
            5
        );

        when(projectRepository.findByIdAndUserId(project.getId(), otherUserId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> projectService.findByIdForUser(project.getId(), otherUserId));

        verify(projectRepository).findByIdAndUserId(project.getId(), otherUserId);
    }

    @Test 
    void shouldRejectProjectThatDoesNotExist() {
        UUID userId = UUID.randomUUID(); 
        UUID projectId = UUID.randomUUID();

        when(projectRepository.findByIdAndUserId(projectId, userId)).thenReturn(Optional.empty());

        assertThrows(ProjectNotFoundException.class, () -> projectService.findByIdForUser(projectId, userId));

        verify(projectRepository).findByIdAndUserId(projectId, userId);
    }

    @Test 
    void shouldCreateProject() {
        UUID userId = UUID.randomUUID();

        Project project = new Project(
            userId, 
            "Orion", 
            "Scheduling app", 
            LocalDateTime.of(2027, 6, 2, 0, 0), 
            5
        );

        when(projectRepository.save(any(Project.class))).thenReturn(project);

        Project result = projectService.createProject(
                userId, 
                "Orion", 
                "Scheduling app", 
                LocalDateTime.of(2027, 6, 1, 0, 0), 
                5
        );

        assertEquals(project, result);

        verify(projectRepository).save(any(Project.class));
    }

    @Test 
    void shouldDeleteProject() {
        UUID userId = UUID.randomUUID();

        Project project = new Project(
            userId, 
            "Orion", 
            "Scheduling app", 
            LocalDateTime.of(2027, 6, 2, 0, 0), 
            5
        );

        when(projectRepository.findByIdAndUserId(project.getId(), userId)).thenReturn(Optional.of(project));

        projectService.deleteProject(project.getId(), userId);

        verify(projectRepository).findByIdAndUserId(project.getId(), userId);

        verify(projectRepository).delete(project);
    }

    @Test 
    void shouldUpdateProject() {
        UUID userId = UUID.randomUUID();

        Project project = new Project(
                userId, 
                "Old Name", 
                "Old description", 
                LocalDateTime.of(2027, 6, 1, 0, 0), 
                3
        );

        when(projectRepository.findByIdAndUserId(project.getId(), userId)).thenReturn(Optional.of(project));

        when(projectRepository.save(project)).thenReturn(project);

        Project result = projectService.updateProject(
                project.getId(), 
                userId, 
                "New Name", 
                "New description", 
                LocalDateTime.of(2027, 7, 1, 0, 0), 
                5
        );

        assertEquals(project, result);
        assertEquals("New Name", result.getName());
        assertEquals("New description", result.getDescription());
        assertEquals(
                LocalDateTime.of(2027, 7, 1, 0, 0), 
                result.getDeadline()
        );
        assertEquals(5, result.getPriority());

        verify(projectRepository).findByIdAndUserId(project.getId(), userId);
        verify(projectRepository).save(project);
    }

    @Test 
    void shouldNotDeleteProjectWhenItDoesNotBelongToUser() {
        UUID ownerId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();

        Project project = new Project(
            ownerId, 
            "Orion", 
            "Scheduling app", 
            LocalDateTime.of(2027, 6, 2, 0, 0), 
            5
        );

        when(projectRepository.findByIdAndUserId(project.getId(), otherUserId)).thenReturn(Optional.empty());

        assertThrows(
                ProjectNotFoundException.class, 
                () -> projectService.deleteProject(project.getId(), otherUserId)
        );

        verify(projectRepository, never()).delete(any());
    }
}