package com.orion.repository;

import com.orion.model.Project;
import com.orion.model.Task;
import com.orion.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TaskRepositoryTest {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired 
    private UserRepository userRepository;

    private User createUser(String email) {
        User user = new User(
                email, 
                "hashed-password"
        );

        return userRepository.save(user);
    }

    private Project createProject(UUID userId, String name) {
        Project project = new Project(
                userId, 
                name, 
                "Test project", 
                LocalDate.of(2027, 6, 1), 
                5
        );

        return projectRepository.save(project);
    }

    private Task createTask(
            UUID projectId, 
            String title
    ) {
        return new Task(
                projectId, 
                title, 
                "Test task", 
                60, 
                LocalDate.of(2027, 5, 1), 
                3
        );
    }

    @Test 
    void shouldSaveAndFindTaskByProject() {
        User user = createUser("task-save@example.com");
        Project project = createProject(
                user.getId(), 
                "Orion"
        );

        Task task = createTask(
                project.getId(), 
                "Implement scheduler"
        );

        taskRepository.save(task);

        List<Task> result = taskRepository.findByProjectId(project.getId());

        assertEquals(1, result.size());
        assertEquals("Implement scheduler", result.get(0).getTitle());
        assertEquals(project.getId(), result.get(0).getProjectId());
    }

    @Test 
    void shoudlFindAllTasksForProject() {
        User user = createUser("multiple-tasks@example.com");
        Project project = createProject(
                user.getId(), 
                "Orion"
        );

        Task task1 = createTask(
                project.getId(), 
                "Design database"
        );

        Task task2 = createTask(
                project.getId(), 
                "Implement API"
        );

        Task task3 = createTask(
                project.getId(), 
                "Write tests"
        );

        taskRepository.save(task1);
        taskRepository.save(task2);
        taskRepository.save(task3);

        List<Task> result = taskRepository.findByProjectId(project.getId());

        assertEquals(3, result.size());

        assertTrue(
                result.stream()
                        .anyMatch(task -> 
                                task.getTitle().equals("Design database"))
        );

        assertTrue(
                result.stream()
                        .anyMatch(task -> 
                                task.getTitle().equals("Implement API"))
        );

        assertTrue(
                result.stream()
                        .anyMatch(task -> 
                                task.getTitle().equals("Write tests"))
        );
    }

    @Test 
    void shouldNotReturnTasksFromAnotherProject() {
        User user = createUser("project-isolation@example.com");

        Project projectA = createProject(
                user.getId(), 
                "Project A"
        );

        Project projectB = createProject(
                user.getId(), 
                "Project B"
        );

        Task taskA = createTask(
                projectA.getId(),
                "Task A"
        );

        Task taskB = createTask(
                projectB.getId(),
                "Task B"
        );

        taskRepository.save(taskA);
        taskRepository.save(taskB);

        List<Task> result = taskRepository.findByProjectId(projectA.getId());

        assertEquals(1, result.size());
        assertEquals("Task A", result.get(0).getTitle());
        assertEquals(projectA.getId(), result.get(0).getProjectId());

    }

    @Test 
    void shouldFindTaskByIdAndProject() {
        User user = createUser("find-task@example.com");

        Project project = createProject(
                user.getId(), 
                "Orion"
        );

        Task task = createTask(
                project.getId(), 
                "Find this task"
        );

        taskRepository.save(task);

        Optional<Task> result = 
                taskRepository.findByIdAndProjectId(
                        task.getId(), 
                        project.getId()
        );

        assertTrue(result.isPresent());
        assertEquals(task.getId(), result.get().getId());
        assertEquals("Find this task", result.get().getTitle());
    }

    @Test 
    void shouldReturnEmptyWhenTaskDoesNotExist() {
        User user = createUser("missing-task@example.com");

        Project project = createProject(
                user.getId(), 
                "Orion"
        );

        UUID taskId = UUID.randomUUID();

        Optional<Task> result = 
                taskRepository.findByIdAndProjectId(
                        taskId, 
                        project.getId()
                );
        
        assertTrue(result.isEmpty());
    }

    @Test 
    void shouldNotFindTaskWhenItBelongsToAnotherProject() {
        User user = createUser("wrong-project@example.com");

        Project projectA = createProject(
                user.getId(), 
                "Project A"
        );

        Project projectB = createProject(
                user.getId(), 
                "Project B"
        );

        Task task = createTask(
                projectA.getId(),
                "Task A"
        );

        taskRepository.save(task);

        Optional<Task> result = 
                taskRepository.findByIdAndProjectId(
                        task.getId(), 
                        projectB.getId()
                );
        
        assertTrue(result.isEmpty());
    }

    @Test 
    void shouldDeleteTask() {
        User user = createUser("delete-task@example.com");

        Project project = createProject(
                user.getId(),
                "Orion"
        );

        Task task = createTask(
                project.getId(), 
                "Delete this task"
        );

        taskRepository.save(task);

        UUID taskId = task.getId();

        taskRepository.delete(task);
        taskRepository.flush();

        Optional<Task> result = taskRepository.findById(taskId);

        assertTrue(result.isEmpty());
    }
}