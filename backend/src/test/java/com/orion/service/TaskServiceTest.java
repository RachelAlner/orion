package com.orion.service;

import com.orion.exception.TaskNotFoundException;
import com.orion.model.TaskStatus;
import com.orion.model.Task;
import com.orion.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock 
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    private UUID projectId;
    private UUID otherProjectId;

    @BeforeEach
    void setUp() {
        projectId = UUID.randomUUID();
        otherProjectId = UUID.randomUUID();
    }

    private Task createTask(String title) {
        return new Task(
                projectId, 
                title, 
                "Test description", 
                60, 
                LocalDate.of(2027, 5, 1), 
                3
        );
    }

    @Test 
    void shouldCreateTask() {
        Task task = createTask("Implement scheduler");

        when(taskRepository.save(any(Task.class))).thenReturn(task);

        Task result = taskService.createTask(
                projectId, 
                "Implement scheduler", 
                "Test description", 
                60, 
                LocalDate.of(2027, 5, 1), 
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

        verify(taskRepository).save(any(Task.class));
    }

    @Test 
    void shouldFindAllTasksForProject() {
        Task task1 = createTask("Design database");
        Task task2 = createTask("Implement API");

        when(taskRepository.findByProjectId(projectId)).thenReturn(List.of(task1, task2));

        List<Task> result = taskService.findAllForProject(projectId);

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
        when(taskRepository.findByProjectId(projectId)).thenReturn(List.of());

        List<Task> result = taskService.findAllForProject(projectId);

        assertTrue(result.isEmpty());

        verify(taskRepository).findByProjectId(projectId);
    }

    @Test 
    void shouldFindTaskForProject() {
        Task task = createTask("Find this task");

        when(taskRepository.findByIdAndProjectId(
                task.getId(),
                projectId
        )).thenReturn(Optional.of(task));

        Task result = taskService.findByIdForProject(
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
        Task task = createTask("Task A");

        when(taskRepository.findByIdAndProjectId(
                task.getId(), 
                otherProjectId
        )).thenReturn(Optional.empty());

        assertThrows(
                TaskNotFoundException.class,
                () -> taskService.findByIdForProject(
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
        UUID taskId = UUID.randomUUID();

        when(taskRepository.findByIdAndProjectId(
                taskId, 
                projectId
        )).thenReturn(Optional.empty());

        assertThrows(
                TaskNotFoundException.class, 
                () -> taskService.findByIdForProject(
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
        Task task = createTask("Old title");

        when(taskRepository.findByIdAndProjectId(
                task.getId(), 
                projectId
        )).thenReturn(Optional.of(task));

        when(taskRepository.save(task)).thenReturn(task);

        Task result = 
                taskService.updateTask(
                        task.getId(), 
                        projectId, 
                        "Updated title", 
                        "Updated description", 
                        120, 
                        LocalDate.of(2027, 6, 1), 
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
                LocalDate.of(2027, 6, 1), 
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
        Task task = createTask("Complete this task");

        when(taskRepository.findByIdAndProjectId(
                task.getId(), 
                projectId
        )).thenReturn(Optional.of(task));

        when(taskRepository.save(task)).thenReturn(task);

        Task result = taskService.completeTask(
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
        Task task = createTask("Delete this task");

        when(taskRepository.findByIdAndProjectId(
                task.getId(), 
                projectId
        )).thenReturn(Optional.of(task));

        taskService.deleteTask(
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

        when(taskRepository.findByIdAndProjectId(
                task.getId(), 
                otherProjectId
        )).thenReturn(Optional.empty());

        assertThrows(
                TaskNotFoundException.class, 
                () -> taskService.deleteTask(
                    task.getId(), 
                    otherProjectId
                )
        );

        verify(taskRepository)
                .findByIdAndProjectId(
                        task.getId(), 
                        otherProjectId
                );
        
        verify(taskRepository, never()).delete(any(Task.class));
    }
}