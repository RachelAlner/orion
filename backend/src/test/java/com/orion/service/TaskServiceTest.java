package com.orion.service;

import com.orion.exception.TaskNotFoundException;
import com.orion.model.TaskStatus;
import com.orion.model.Task;
import com.orion.model.Project;
import com.orion.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock 
    private TaskRepository taskRepository;

    @Mock 
    private ProjectService projectService;

    @InjectMocks
    private TaskService taskService;

    private UUID userId;
    private UUID projectId;
    private UUID otherProjectId;

    private Project project;
    private Project otherProject;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        project = new Project(
                userId, 
                "Test Project",
                "Test description", 
                LocalDateTime.of(2027, 9, 2, 0, 0),
                4
        );

        projectId = project.getId();

        otherProject = new Project(
                userId, 
                "Other Test Project",
                "Test description", 
                LocalDateTime.of(2027, 9, 2, 0, 0),
                4
        );

        otherProjectId = otherProject.getId();
    }

    private Task createTask(String title) {
        return new Task(
                projectId, 
                title, 
                "Test description", 
                60, 
                LocalDateTime.of(2027, 5, 1, 0, 0), 
                3
        );
    }

    @Test 
    void shouldCreateTask() {
        when(projectService.findByIdForUser(projectId, userId))
                .thenReturn(project);

        when(taskRepository.save(any(Task.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Task result = taskService.createTask(
                userId, 
                projectId, 
                "Implement scheduler", 
                "Test description", 
                60, 
                LocalDateTime.of(2027, 5, 1, 0, 0), 
                3
        );

        assertNotNull(result);
        assertEquals(
                "Implement scheduler", 
                result.getTitle()
        );
        assertEquals(
                projectId, 
                result.getProjectId()
        );

        verify(projectService).findByIdForUser(projectId, userId);
        verify(taskRepository).save(any(Task.class));
    }

    @Test 
    void shouldFindAllTasksForProject() {
        when(projectService.findByIdForUser(projectId, userId))
                .thenReturn(project);

        Task task1 = createTask("Design database");
        Task task2 = createTask("Implement API");

        when(taskRepository.findByProjectId(projectId)).thenReturn(List.of(task1, task2));

        List<Task> result = taskService.findAllForProject(userId, projectId);

        assertEquals(2, result.size());
        assertEquals(
                "Design database", 
                result.get(0).getTitle()
        );
        assertEquals(
                "Implement API", 
                result.get(1).getTitle()
        );

        verify(taskRepository).findByProjectId(projectId);
    }

    @Test 
    void shouldReturnEmptyListWhenProjectHasNoTasks() {
        when(projectService.findByIdForUser(projectId, userId))
                .thenReturn(project);


        when(taskRepository.findByProjectId(projectId)).thenReturn(List.of());

        List<Task> result = taskService.findAllForProject(userId, projectId);

        assertTrue(result.isEmpty());

        verify(taskRepository).findByProjectId(projectId);
    }

    @Test 
    void shouldFindTaskForProject() {
        when(projectService.findByIdForUser(projectId, userId))
                .thenReturn(project);

        Task task = createTask("Find this task");

        when(taskRepository.findByIdAndProjectId(
                task.getId(),
                projectId
        )).thenReturn(Optional.of(task));

        Task result = taskService.findByIdForProject(
                userId, 
                task.getId(),
                projectId
        );

        assertNotNull(result);
        assertEquals(task.getId(), result.getId());
        assertEquals("Find this task", result.getTitle());
        assertEquals(projectId, result.getProjectId());

        verify(taskRepository).findByIdAndProjectId(
                task.getId(), 
                projectId
        );
    }

    @Test 
    void shouldRejectTaskThatDoesNotBelongToProject() {
        when(projectService.findByIdForUser(otherProjectId, userId))
                .thenReturn(otherProject);

        Task task = createTask("Task A");

        when(taskRepository.findByIdAndProjectId(
                task.getId(), 
                otherProjectId
        )).thenReturn(Optional.empty());

        assertThrows(
                TaskNotFoundException.class,
                () -> taskService.findByIdForProject(
                        userId, 
                        task.getId(), 
                        otherProjectId
                )
        );

        verify(taskRepository).findByIdAndProjectId(
                task.getId(), 
                otherProjectId
        );
    }

    @Test 
    void shouldRejectTaskThatDoesNotExist() {
        when(projectService.findByIdForUser(projectId, userId))
                .thenReturn(project);

        UUID taskId = UUID.randomUUID();

        when(taskRepository.findByIdAndProjectId(
                taskId, 
                projectId
        )).thenReturn(Optional.empty());

        assertThrows(
                TaskNotFoundException.class, 
                () -> taskService.findByIdForProject(
                        userId, 
                        taskId, 
                        projectId
                )
        );

        verify(taskRepository).findByIdAndProjectId(
                taskId, 
                projectId
        );
    }

    @Test 
    void shouldUpdateTask() {
        when(projectService.findByIdForUser(projectId, userId))
                .thenReturn(project);

        Task task = createTask("Old title");

        when(taskRepository.findByIdAndProjectId(
                task.getId(), 
                projectId
        )).thenReturn(Optional.of(task));

        when(taskRepository.save(task)).thenReturn(task);

        Task result = 
                taskService.updateTask(
                        userId, 
                        task.getId(), 
                        projectId, 
                        "Updated title", 
                        "Updated description", 
                        120, 
                        LocalDateTime.of(2027, 6, 1, 0, 0), 
                        5
                );

        assertEquals(
                "Updated title", 
                result.getTitle()
        );

        assertEquals(
                "Updated description", 
                result.getDescription()
        );

        assertEquals(
                120, 
                result.getEstimatedMinutes()
        );
        assertEquals(
                LocalDateTime.of(2027, 6, 1, 0, 0), 
                result.getDeadline()
        );
        assertEquals(
                5, 
                result.getPriority()
        );

        verify(taskRepository).findByIdAndProjectId(
                task.getId(), 
                projectId
        );

        verify(taskRepository).save(task);
    }

    @Test 
    void shouldCompleteTask() {
        when(projectService.findByIdForUser(projectId, userId))
                .thenReturn(project);

        Task task = createTask("Complete this task");

        when(taskRepository.findByIdAndProjectId(
                task.getId(), 
                projectId
        )).thenReturn(Optional.of(task));

        when(taskRepository.save(task)).thenReturn(task);

        Task result = taskService.completeTask(
                userId, 
                task.getId(), 
                projectId
        );

        assertEquals(
                TaskStatus.COMPLETED, 
                result.getStatus()
        );
        assertNotNull(result.getCompletedAt());

        verify(taskRepository).findByIdAndProjectId(
                task.getId(), 
                projectId
        );

        verify(taskRepository).save(task);
    }

    @Test 
    void shouldDeleteTask() {
        when(projectService.findByIdForUser(projectId, userId))
                .thenReturn(project);

        Task task = createTask("Delete this task");

        when(taskRepository.findByIdAndProjectId(
                task.getId(), 
                projectId
        )).thenReturn(Optional.of(task));

        taskService.deleteTask(
                userId, 
                task.getId(), 
                projectId
        );

        verify(taskRepository).findByIdAndProjectId(
                task.getId(), 
                projectId
        );

        verify(taskRepository).delete(task);
    }

    @Test 
    void shouldNotDeleteTaskWhenItDoesNotBelongToProject() {
        Task task = createTask("Protected task");

        when(projectService.findByIdForUser(otherProjectId, userId))
                .thenReturn(otherProject);

        when(taskRepository.findByIdAndProjectId(
                task.getId(), 
                otherProjectId
        )).thenReturn(Optional.empty());

        assertThrows(
                TaskNotFoundException.class, 
                () -> taskService.deleteTask(
                    userId, 
                    task.getId(), 
                    otherProjectId
                )
        );

        verify(projectService, times(2))
                .findByIdForUser(
                        otherProjectId, 
                        userId 
                );

        verify(taskRepository)
                .findByIdAndProjectId(
                        task.getId(), 
                        otherProjectId
                );
        
        verify(taskRepository, never()).delete(any(Task.class));
    }

    @Test 
    void recordProgressUpdateTask() {
        Task task = createTask("Progress task");

        when(projectService.findByIdForUser(projectId, userId))
                .thenReturn(project);
        
        when(taskRepository.findByIdAndProjectId(
                task.getId(), 
                projectId
        )).thenReturn(Optional.of(task));

        when(taskRepository.save(task)).thenReturn(task);

        Task result = taskService.recordProgress(
                userId, 
                task.getId(), 
                projectId, 
                30
        );

        assertSame(task, result);

        assertEquals(
                30, 
                task.getRemainingMinutes()
        );

        assertEquals(
                TaskStatus.IN_PROGRESS,
                task.getStatus()
        );

        verify(taskRepository).save(task);
    }

    @Test 
    void recordProgressCompletesTaskWhenRemainingReachesZero() {
        Task task = createTask("Progress task");

        when(projectService.findByIdForUser(projectId, userId))
                .thenReturn(project);
        
        when(taskRepository.findByIdAndProjectId(
                task.getId(), 
                projectId
        )).thenReturn(Optional.of(task));

        when(taskRepository.save(task)).thenReturn(task);

        Task result = taskService.recordProgress(
                userId, 
                task.getId(), 
                projectId, 
                60
        );

        assertSame(task, result);

        assertEquals(
                0, 
                task.getRemainingMinutes()
        );

        assertEquals(
                TaskStatus.COMPLETED,
                task.getStatus()
        );

        assertNotNull(task.getCompletedAt());

        verify(taskRepository).save(task);
    }

    @Test 
    void recordProgressRejectsCompletedTask() {
        Task task = createTask("Progress task");

        task.complete();

        when(projectService.findByIdForUser(projectId, userId))
                .thenReturn(project);
        
        when(taskRepository.findByIdAndProjectId(
                task.getId(), 
                projectId
        )).thenReturn(Optional.of(task));

        assertThrows(
                IllegalStateException.class, 
                () -> taskService.recordProgress(
                        userId, 
                        task.getId(), 
                        projectId, 
                        30
                )
        );

        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test 
    void recordProgressUsesCorrectUserAndProject() {
        Task task = createTask("Progress task");

        when(projectService.findByIdForUser(projectId, userId))
                .thenReturn(project);

        when(taskRepository.findByIdAndProjectId(
                task.getId(),
                projectId
        )).thenReturn(Optional.of(task));
        
        when(taskRepository.save(task)).thenReturn(task);
        

        Task result = taskService.recordProgress(
                userId, 
                task.getId(), 
                projectId, 
                20
        );

        assertSame(task, result);

        assertEquals(
                projectId, 
                task.getProjectId()
        );

        assertEquals(
                40, 
                task.getRemainingMinutes()
        );

        verify(projectService).findByIdForUser(
                projectId, 
                userId
        );

        verify(taskRepository).findByIdAndProjectId(
                task.getId(),
                projectId
        );

        verify(taskRepository).save(task);
    }
}