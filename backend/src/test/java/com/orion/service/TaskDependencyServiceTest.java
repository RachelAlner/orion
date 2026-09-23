package com.orion.service;

import com.orion.exception.DependencyCycleException;
import com.orion.exception.InvalidTaskDependencyException;
import com.orion.exception.TaskDependencyAlreadyExistsException;
import com.orion.exception.TaskDependencyNotFoundException;
import com.orion.model.Task;
import com.orion.model.TaskDependency;
import com.orion.model.TaskDependencyId;
import com.orion.model.Project;
import com.orion.model.User;
import com.orion.repository.TaskDependencyRepository;
import com.orion.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskDependencyServiceTest {

    @Mock 
    private TaskDependencyRepository taskDependencyRepository;

    @Mock 
    private TaskRepository taskRepository;

    @Mock 
    private ProjectService projectService;

    private TaskDependencyService taskDependencyService;

    private UUID userId;
    private UUID projectId;

    @BeforeEach
    void setUp() {
        taskDependencyService = 
                new TaskDependencyService(
                        taskDependencyRepository, 
                        taskRepository, 
                        projectService
                );
        
        userId = UUID.randomUUID();
        projectId = UUID.randomUUID();
    }

    @Test 
    void shouldAddTaskDependency() {
        Task task = createTask(projectId, "Implement API");

        UUID taskId = task.getId();

        Task prerequisite = 
                createTask(
                        projectId, 
                        "Design database"
                );

        UUID prerequisiteTaskId = prerequisite.getId();
        
        TaskDependency dependency = 
                new TaskDependency(
                        new TaskDependencyId(
                                taskId, 
                                prerequisiteTaskId
                        )
                );
        
        when(taskRepository.findByIdAndProjectId(taskId, projectId))
                .thenReturn(Optional.of(task));
        
        when(taskRepository.findById(prerequisiteTaskId))
                .thenReturn(Optional.of(prerequisite));
        
        when(taskDependencyRepository
                .existsByIdTaskIdAndIdDependsOnTaskId(
                        taskId, 
                        prerequisiteTaskId
                ))
                .thenReturn(false);
        
        when(taskDependencyRepository.findByIdTaskId(any(UUID.class)))
                .thenReturn(List.of());

        when(taskDependencyRepository.save(any(TaskDependency.class)))
                .thenReturn(dependency);

        TaskDependency result =
                taskDependencyService.addDependency(
                        userId, 
                        projectId, 
                        taskId, 
                        prerequisiteTaskId
                );

        assertNotNull(result);
        assertEquals(
                taskId, 
                result.getId().getTaskId()
        );
        assertEquals(
                prerequisiteTaskId, 
                result.getId().getDependsOnTaskId()
        );

        verify(taskDependencyRepository).save(any(TaskDependency.class));
    }

    @Test 
    void shouldRejectDependencyWhenTaskDoesNotBelongToProject() {
        UUID taskId = UUID.randomUUID();
        UUID prerequisiteTaskId = UUID.randomUUID();

        when(taskRepository.findByIdAndProjectId(taskId, projectId))
                .thenReturn(Optional.empty());

        assertThrows(
                InvalidTaskDependencyException.class, 
                () -> taskDependencyService.addDependency(
                        userId, 
                        projectId, 
                        taskId, 
                        prerequisiteTaskId
                )
        );

        verify(taskDependencyRepository, never()).save(any(TaskDependency.class));
    }

    @Test 
    void shouldRejectDependencyWhenPrerequisiteTaskDoesNotExist() {
        Task task = createTask(
                projectId, 
                "Implement API"
        );

        UUID taskId = task.getId();
        UUID prerequisiteTaskId = UUID.randomUUID();

        when(taskRepository.findByIdAndProjectId(taskId, projectId))
                .thenReturn(Optional.of(task));
        
        when(taskRepository.findById(prerequisiteTaskId))
                .thenReturn(Optional.empty());
        
        assertThrows(
                InvalidTaskDependencyException.class, 
                () -> taskDependencyService.addDependency(
                        userId, 
                        projectId, 
                        taskId, 
                        prerequisiteTaskId
                )
        );

        verify(taskDependencyRepository, never())
                .save(any(TaskDependency.class));
    }

    @Test 
    void shouldRejectSelfDependency() {
        Task task = createTask(
                projectId, 
                "Implement API"
        );

        UUID taskId = task.getId();

        when(taskRepository.findByIdAndProjectId(taskId, projectId))
                .thenReturn(Optional.of(task));

        assertThrows(
                InvalidTaskDependencyException.class, 
                () -> taskDependencyService.addDependency(
                        userId, 
                        projectId, 
                        taskId, 
                        taskId
                )
        );

        verify(taskDependencyRepository, never())
                .save(any(TaskDependency.class));
    }

    @Test 
    void shouldRejectDuplicateDependency() {
        Task task = createTask(
                projectId, 
                "Implement API"
        );

        UUID taskId = task.getId();

        Task prerequisite = 
                createTask(
                        projectId, 
                        "Design database"
                );

        UUID prerequisiteTaskId = prerequisite.getId();
        
        when(taskRepository.findByIdAndProjectId(taskId, projectId))
                .thenReturn(Optional.of(task));
        
        when(taskRepository.findById(prerequisiteTaskId))
                .thenReturn(Optional.of(prerequisite));

        when(taskDependencyRepository
                .existsByIdTaskIdAndIdDependsOnTaskId(
                        taskId, 
                        prerequisiteTaskId
                ))
                .thenReturn(true);
        
        assertThrows(
                TaskDependencyAlreadyExistsException.class, 
                () -> taskDependencyService.addDependency(
                        userId, 
                        projectId, 
                        taskId, 
                        prerequisiteTaskId
                )
        );

        verify(taskDependencyRepository, never())
                .save(any(TaskDependency.class));
    }

    @Test 
    void shouldRejectDirectCycle() {
        Task task = createTask(
                projectId, 
                "Task A"
        );

        UUID taskId = task.getId();

        Task prerequisite = createTask(
                projectId, 
                "Task B"
        );

        UUID prerequisiteTaskId = prerequisite.getId();

        when(taskRepository.findByIdAndProjectId(taskId, projectId))
                .thenReturn(Optional.of(task));

        when(taskRepository.findById(prerequisiteTaskId))
                .thenReturn(Optional.of(prerequisite));

        
        when(taskDependencyRepository
                .existsByIdTaskIdAndIdDependsOnTaskId(
                        taskId, 
                        prerequisiteTaskId
                ))
                .thenReturn(false);
        
        TaskDependency existingDependency = 
                new TaskDependency(
                        new TaskDependencyId(
                                prerequisiteTaskId, 
                                taskId
                        )
                );
        
        when(taskDependencyRepository.findByIdTaskId(prerequisiteTaskId))
                .thenReturn(List.of(existingDependency));
        
        assertThrows(
                DependencyCycleException.class, 
                () -> taskDependencyService.addDependency(
                        userId, 
                        projectId, 
                        taskId, 
                        prerequisiteTaskId
                )
        );

        verify(taskDependencyRepository, never())
                .save(any(TaskDependency.class));
    }

    @Test
    void shouldRejectIndirectCycle() {

        Task taskA = 
                createTask(
                        projectId, 
                        "Task A"
                );

        Task taskB = 
                createTask(
                        projectId, 
                        "Task B"
                );

        Task taskC = 
                createTask(
                        projectId, 
                        "Task C"
                );

        UUID taskAId = taskA.getId();
        UUID taskBId = taskB.getId();
        UUID taskCId = taskC.getId();

        // existing:
        // B -> A
        // C -> B

        // attempting:
        // A -> C

        // would create:
        // A -> C -> B -> A

        when(taskRepository.findByIdAndProjectId(taskAId, projectId))
                .thenReturn(Optional.of(taskA));
        
        when(taskRepository.findById(taskCId))
                .thenReturn(Optional.of(taskC));
        
        when(taskDependencyRepository
                .existsByIdTaskIdAndIdDependsOnTaskId(
                        taskAId, 
                        taskCId
                ))
                .thenReturn(false);
        
        TaskDependency cDependsOnB = 
                new TaskDependency(
                        new TaskDependencyId(
                                taskCId, 
                                taskBId
                        )
                );

        TaskDependency bDependsOnA = 
                new TaskDependency(
                        new TaskDependencyId(
                                taskBId, 
                                taskAId
                        )
                );

        when(taskDependencyRepository.findByIdTaskId(taskCId))
                .thenReturn(List.of(cDependsOnB));
        
        when(taskDependencyRepository.findByIdTaskId(taskBId))
                .thenReturn(List.of(bDependsOnA));

        assertThrows(
                DependencyCycleException.class, 
                () -> taskDependencyService.addDependency(
                        userId, 
                        projectId, 
                        taskAId, 
                        taskCId
                )
        );

        verify(taskDependencyRepository, never())
                .save(any(TaskDependency.class));
    }

    @Test 
    void shouldAllowValidDependencyChain() {

        Task taskA = 
                createTask(
                        projectId, 
                        "Task A"
                );

        Task taskB = 
                createTask(
                        projectId, 
                        "Task B"
                );

        Task taskC = 
                createTask(
                        projectId, 
                        "Task C"
                );

        UUID taskAId = taskA.getId();
        UUID taskBId = taskB.getId();
        UUID taskCId = taskC.getId();

        // existing:
        // B -> A

        // adding: 
        // C -> B 

        // produces: 
        // C -> B -> A 

        // which is valid 

        when(taskRepository.findByIdAndProjectId(taskCId, projectId))
                .thenReturn(Optional.of(taskC));

        when(taskRepository.findById(taskBId))
                .thenReturn(Optional.of(taskB));

        when(taskDependencyRepository
                .existsByIdTaskIdAndIdDependsOnTaskId(
                        taskCId, 
                        taskBId
                ))
                .thenReturn(false);

        TaskDependency bDependsOnA = 
                new TaskDependency(
                        new TaskDependencyId(
                                taskBId, 
                                taskAId
                        )
                );

        when(taskDependencyRepository.findByIdTaskId(taskBId))
                .thenReturn(List.of(bDependsOnA));

        when(taskDependencyRepository.findByIdTaskId(taskAId))
                .thenReturn(List.of());
        
        TaskDependency savedDependency = 
                new TaskDependency(
                        new TaskDependencyId(
                                taskCId, 
                                taskBId
                        )
                );

        when(taskDependencyRepository.save(any(TaskDependency.class)))
                .thenReturn(savedDependency);

        TaskDependency result = 
                taskDependencyService.addDependency(
                        userId, 
                        projectId, 
                        taskCId, 
                        taskBId
                );

        assertNotNull(result);

        assertEquals(
                taskCId, 
                result.getId().getTaskId()
        );

        assertEquals(
                taskBId, 
                result.getId().getDependsOnTaskId()
        );

        verify(taskDependencyRepository)
                .save(any(TaskDependency.class));
    }

    @Test 
    void shouldFindDependenciesForTask() {

        Task task = 
                createTask(
                        projectId, 
                        "Implement API"
                );

        UUID taskId = task.getId();

        Task prerequisite = 
                createTask(
                        projectId, 
                        "Design database"
                );

        UUID prerequisiteTaskId = prerequisite.getId();

        TaskDependency dependency = 
                new TaskDependency(
                        new TaskDependencyId(
                                taskId, 
                                prerequisiteTaskId
                        )
                );

        when(taskRepository.findByIdAndProjectId(taskId, projectId))
                .thenReturn(Optional.of(task));

        when(taskDependencyRepository.findByIdTaskId(taskId))
                .thenReturn(List.of(dependency));

        List<TaskDependency> result = 
                taskDependencyService.findDependencies(
                        userId, 
                        projectId, 
                        taskId
                );

        assertEquals(1, result.size());
        assertEquals(
                taskId, 
                result.get(0).getId().getTaskId()
        );

        assertEquals(
                prerequisiteTaskId, 
                result.get(0)
                        .getId()
                        .getDependsOnTaskId()
        );

        verify(taskDependencyRepository)
                .findByIdTaskId(taskId);
    }

    @Test 
    void shouldReturnEmptyListWhenTaskHasNoDependencies() {
        Task task = 
                createTask(
                    projectId, 
                    "Standalone task"
                );
        
        UUID taskId = task.getId();

        when(taskRepository.findByIdAndProjectId(taskId, projectId))
                .thenReturn(Optional.of(task));

        when(taskDependencyRepository.findByIdTaskId(taskId))
                .thenReturn(List.of());

        List<TaskDependency> result = 
                taskDependencyService.findDependencies(
                        userId, 
                        projectId, 
                        taskId
                );
        
        assertTrue(result.isEmpty());

        verify(taskDependencyRepository)
                .findByIdTaskId(taskId);
    }

    @Test 
    void shouldRejectFindingDependenciesForTaskNotInProject() {
        UUID taskId = UUID.randomUUID();

        when(taskRepository.findByIdAndProjectId(taskId, projectId))
                .thenReturn(Optional.empty());

        assertThrows(
                InvalidTaskDependencyException.class, 
                () -> taskDependencyService.findDependencies(
                        userId, 
                        projectId, 
                        taskId
                )
        );

        verify(taskDependencyRepository, never())
                .findByIdTaskId(any(UUID.class));
    }

    @Test 
    void shouldRemoveTaskDependency() {
        Task task = 
                createTask(
                        projectId, 
                        "Implement API"
                );
        
        UUID taskId = task.getId();
        
        Task prerequisite = 
                createTask(
                        projectId, 
                        "Design database"
                );

        UUID prerequisiteTaskId = prerequisite.getId();

        TaskDependencyId dependencyId = 
                new TaskDependencyId(
                        taskId, 
                        prerequisiteTaskId
                );
        
        TaskDependency dependency = 
                new TaskDependency(dependencyId);

        when(taskRepository.findByIdAndProjectId(taskId, projectId))
                .thenReturn(Optional.of(task));

        when(taskRepository.findById(prerequisiteTaskId))
                .thenReturn(Optional.of(prerequisite));

        when(taskDependencyRepository.findById(dependencyId))
                .thenReturn(Optional.of(dependency));
        
        taskDependencyService.removeDependency(
                userId, 
                projectId, 
                taskId, 
                prerequisiteTaskId
        );

        verify(taskDependencyRepository)
                .delete(dependency);
    }

    @Test 
    void shouldRejectRemovingDependencyWhenItDoesNotExist() {
        Task task = 
                createTask(
                        projectId, 
                        "Implement API"
                );

        UUID taskId = task.getId();
        
        Task prerequisite = 
                createTask(
                        projectId, 
                        "Design database"
                );

        UUID prerequisiteTaskId = prerequisite.getId();

        TaskDependencyId dependencyId = 
                new TaskDependencyId(
                        taskId, 
                        prerequisiteTaskId
                );
        
        when(taskRepository.findByIdAndProjectId(taskId, projectId))
                .thenReturn(Optional.of(task));

        when(taskRepository.findById(prerequisiteTaskId))
                .thenReturn(Optional.of(prerequisite));

        when(taskDependencyRepository.findById(dependencyId))
                .thenReturn(Optional.empty());

        assertThrows(
                TaskDependencyNotFoundException.class, 
                () -> taskDependencyService.removeDependency(
                        userId, 
                        projectId, 
                        taskId, 
                        prerequisiteTaskId
                )
        );

        verify(taskDependencyRepository, never())
                .delete(any(TaskDependency.class));
    }

    @Test 
    void shouldRejectRemovingDependencyWhenTaskDoesNotBelongToProject() {
        UUID taskId = UUID.randomUUID();
        UUID prerequisiteTaskId = UUID.randomUUID();

        when(taskRepository.findByIdAndProjectId(taskId, projectId))
                .thenReturn(Optional.empty());

        assertThrows(
                InvalidTaskDependencyException.class, 
                () -> taskDependencyService.removeDependency(
                        userId, 
                        projectId, 
                        taskId, 
                        prerequisiteTaskId
                )
        );

        verify(taskDependencyRepository, never())
                .delete(any(TaskDependency.class));
    }

    @Test 
    void shouldCreateDependencyWithCorrectTaskIds() {
        Task task = 
                createTask(
                        projectId, 
                        "Implement API"
                );

        UUID taskId = task.getId();
        
        Task prerequisite = 
                createTask(
                        projectId, 
                        "Design database"
                );

        UUID prerequisiteTaskId = prerequisite.getId();

        when(taskRepository.findByIdAndProjectId(taskId, projectId))
                .thenReturn(Optional.of(task));

        when(taskRepository.findById(prerequisiteTaskId))
                .thenReturn(Optional.of(prerequisite));

        when(taskDependencyRepository
                .existsByIdTaskIdAndIdDependsOnTaskId(
                        taskId, 
                        prerequisiteTaskId
                ))
                .thenReturn(false);
        
        when(taskDependencyRepository.findByIdTaskId(any(UUID.class)))
                .thenReturn(List.of());
        
        ArgumentCaptor<TaskDependency> captor = 
                ArgumentCaptor.forClass(TaskDependency.class);
        
        when(taskDependencyRepository.save(any(TaskDependency.class)))
                .thenAnswer(invocation -> 
                        invocation.getArgument(0));
        
        taskDependencyService.addDependency(
                userId, 
                projectId, 
                taskId, 
                prerequisiteTaskId
        );

        verify(taskDependencyRepository)
                .save(captor.capture());
        
        TaskDependency savedDependency = 
                captor.getValue();
        
        assertEquals(
                taskId, 
                savedDependency.getId().getTaskId()
        );

        assertEquals(
                prerequisiteTaskId, 
                savedDependency.getId().getDependsOnTaskId()
        );
    }

    private Task createTask(UUID projectId, String title) {
        Task task = new Task(
                projectId, 
                title, 
                "Test task", 
                60, 
                LocalDateTime.now().plusDays(7), 
                3
        );

        return task;
    }

}
