package com.orion.repository;

import com.orion.model.Project;
import com.orion.model.Task;
import com.orion.model.TaskDependency;
import com.orion.model.TaskDependencyId;
import com.orion.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TaskDependencyRepositoryTest {

    @Autowired
    private TaskDependencyRepository taskDependencyRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired 
    private ProjectRepository projectRepository;

    @Autowired 
    private UserRepository userRepository;

    private Project project;

    @BeforeEach
    void setUp() {
        User user = new User(
                "dependency-test@example.com", 
                "hashed-password"
        );

        user = userRepository.saveAndFlush(user);

        project = new Project(
                user.getId(), 
                "Test Project", 
                "Project for dependency tests", 
                LocalDate.now().plusDays(30), 
                3
        );

        project = projectRepository.saveAndFlush(project);
    }

    @Test 
    void shouldSaveAndFindTaskDependency() {
        Task task = createTask("Implement API");
        Task prerequisite = createTask("Design database");

        TaskDependencyId id = 
                new TaskDependencyId(
                        task.getId(), 
                        prerequisite.getId()
                );

        TaskDependency dependency = new TaskDependency(id);

        taskDependencyRepository.saveAndFlush(dependency);

        TaskDependency found = taskDependencyRepository.findById(id).orElseThrow();

        assertEquals(task.getId(), found.getId().getTaskId());
        assertEquals(prerequisite.getId(), found.getId().getDependsOnTaskId());

    }

    @Test 
    void shouldFindDependenciesForTask() {
        Task task = createTask("Implement API");
        Task prerequisite1 = createTask("Design database");
        Task prerequisite2 = createTask("Design authentication");
        Task unrelatedTask = createTask("Write documentation");

        saveDependency(task, prerequisite1);
        saveDependency(task, prerequisite2);
        saveDependency(unrelatedTask, prerequisite1);

        List<TaskDependency> dependencies = 
                taskDependencyRepository.findByIdTaskId(task.getId());

        assertEquals(2, dependencies.size());

        assertTrue(
                dependencies.stream()
                        .anyMatch(dependency -> 
                                dependency.getId()
                                        .getDependsOnTaskId()
                                        .equals(prerequisite1.getId()))
        );

        assertTrue(
                dependencies.stream()
                        .anyMatch(dependency -> 
                                dependency.getId()
                                        .getDependsOnTaskId()
                                        .equals(prerequisite2.getId()))
        );
    }

    @Test 
    void shouldFindTasksThatDependOnAnotherTask() {
        Task task1 = createTask("Implement API");
        Task task2 = createTask("Write tests");
        Task prerequisite = createTask("Design database");

        saveDependency(task1, prerequisite);
        saveDependency(task2, prerequisite);

        List<TaskDependency> dependents = 
                taskDependencyRepository
                        .findByIdDependsOnTaskId(prerequisite.getId());
        
        assertEquals(2, dependents.size());

        assertTrue(
                dependents.stream()
                        .anyMatch(dependency -> 
                                dependency.getId()
                                        .getTaskId()
                                        .equals(task1.getId()))
        );

        assertTrue(
                dependents.stream()
                        .anyMatch(dependency -> 
                                dependency.getId()
                                        .getTaskId()
                                        .equals(task2.getId()))
        );
    }

    @Test 
    void shouldReturnEmptyListWhenNoTasksDependOnTask() {
        Task task = createTask("Standalone task");

        List<TaskDependency> dependents = 
                taskDependencyRepository
                        .findByIdDependsOnTaskId(task.getId());

        assertTrue(dependents.isEmpty());
    }

    @Test 
    void shouldReturnTrueWhenDependencyExists() {
        Task task = createTask("Implement API");
        Task prerequisite = createTask("Design database");

        saveDependency(task, prerequisite);

        assertTrue(
                taskDependencyRepository
                        .existsByIdTaskIdAndIdDependsOnTaskId(
                                task.getId(),
                                prerequisite.getId()
                        )
        );
    }

    @Test 
    void shouldReturnFalseWhenDependencyDoesNotExist() {
        Task task = createTask("Implement API");
        Task prerequisite = createTask("Design database");

        assertFalse(
                taskDependencyRepository
                        .existsByIdTaskIdAndIdDependsOnTaskId(
                                task.getId(), 
                                prerequisite.getId()
                        )
        );
    }

    @Test 
    void shouldDeleteTaskDependency() {
        Task task = createTask("Implement API");
        Task prerequisite = createTask("Design database");

        TaskDependency dependency = 
                saveDependency(task, prerequisite);
        
        TaskDependencyId id = dependency.getId();

        taskDependencyRepository.delete(dependency);
        taskDependencyRepository.flush();

        assertFalse(
                taskDependencyRepository.findById(id).isPresent()
        );
    }


    private Task createTask(String title) {
        Task task = new Task(
                project.getId(), 
                title, 
                "Test task", 
                60, 
                LocalDate.now().plusDays(7), 
                3
        );

        return taskRepository.saveAndFlush(task);
    }

    private TaskDependency saveDependency(
            Task task, 
            Task prerequisite
    ) {
        TaskDependency dependency = 
                new TaskDependency(
                    new TaskDependencyId(
                            task.getId(), 
                            prerequisite.getId()
                    )
            );

        return taskDependencyRepository.saveAndFlush(dependency);
    }
    
}