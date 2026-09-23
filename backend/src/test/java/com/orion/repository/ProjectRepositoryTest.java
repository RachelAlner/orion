package com.orion.repository;

import com.orion.model.Project;
import com.orion.model.ProjectStatus;
import com.orion.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest 
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProjectRepositoryTest {

    @Autowired 
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Test 
    void shouldSaveAndFindProjectByUser() {
        User user = createUser();

        userRepository.save(user);

        Project project = new Project(
            user.getId(), 
            "Orion", 
            "Adaptive scheduling app", 
            LocalDateTime.of(2027, 6, 1, 0, 0), 
            5
        );

        projectRepository.save(project);

        Optional<Project> result = projectRepository.findByIdAndUserId(project.getId(), user.getId());

        assertTrue(result.isPresent());

        Project savedProject = result.get();

        assertEquals(project.getId(), savedProject.getId());
        assertEquals(user.getId(), savedProject.getUserId());
        assertEquals("Orion", savedProject.getName());
        assertEquals("Adaptive scheduling app", savedProject.getDescription());
        assertEquals(LocalDateTime.of(2027, 6, 1, 0, 0), savedProject.getDeadline());
        assertEquals(5, savedProject.getPriority());
        assertEquals(ProjectStatus.ACTIVE, savedProject.getStatus());
    }

    @Test 
    void shouldFindAllProjectsForUser() {
        User user = createUser();

        userRepository.save(user);

        Project firstProject = new Project(
            user.getId(), 
            "Orion", 
            "Scheduling app",
            LocalDateTime.of(2027, 6, 1, 0, 0), 
            5
        );

        Project secondProject = new Project(
            user.getId(), 
            "Database", 
            "Database project", 
            LocalDateTime.of(2027, 5, 20, 0, 0), 
            4
        );

        projectRepository.save(firstProject);
        projectRepository.save(secondProject);

        List<Project> projects = projectRepository.findByUserId(user.getId());

        assertEquals(2, projects.size());

        assertTrue(projects.stream()
                            .anyMatch(project -> project.getName().equals("Orion")));
        
        assertTrue(projects.stream()
                            .anyMatch(project -> project.getName().equals("Database")));
        
    }

    @Test 
    void shouldNotReturnProjectsBelongingToAnotherUser() {
        User owner = createUser();

        User otherUser = createUser();

        userRepository.save(owner);
        userRepository.save(otherUser);

        Project project = new Project(
            owner.getId(), 
            "Orion", 
            "Scheduling app", 
            LocalDateTime.of(2027, 6, 1, 0, 0), 
            5
        );

        projectRepository.save(project);

        Optional<Project> result = projectRepository.findByIdAndUserId(project.getId(), otherUser.getId());

        assertTrue(result.isEmpty());
    }

    @Test 
    void shouldReturnEmptyWhenProjectDoesNotExist() {
        User user = createUser();

        userRepository.save(user);

        UUID projectId = UUID.randomUUID();

        Optional<Project> result = projectRepository.findByIdAndUserId(projectId, user.getId());

        assertTrue(result.isEmpty());
    }

    @Test 
    void shouldOnlyReturnProjectsBelongingToRequestedUser() {
        User firstUser = createUser();

        User secondUser = createUser();

        userRepository.save(firstUser);
        userRepository.save(secondUser);

        Project firstProject = new Project(
                firstUser.getId(), 
                "First Project", 
                "Owned by first user", 
                LocalDateTime.of(2027, 6, 1, 0, 0), 
                3
        ); 

        Project secondProject = new Project(
                secondUser.getId(), 
                "Second Project", 
                "Owned by second user", 
                LocalDateTime.of(2027, 6, 2, 0, 0), 
                4
        );

        projectRepository.save(firstProject);
        projectRepository.save(secondProject);

        List<Project> firstUserProjects = projectRepository.findByUserId(firstUser.getId());

        assertEquals(1, firstUserProjects.size());
        assertEquals("First Project", firstUserProjects.get(0).getName());
        assertEquals(firstUser.getId(), firstUserProjects.get(0).getUserId());
    }

    @Test 
    void shouldDeleteProject() {
        User user = createUser();

        userRepository.save(user);

        Project project = new Project(
            user.getId(), 
            "Project to Delete", 
            "Temporary project", 
            LocalDateTime.of(2027, 6, 1, 0, 0), 
            3
        ); 

        projectRepository.save(project);

        UUID projectId = project.getId();

        assertTrue(projectRepository.findById(projectId).isPresent());

        projectRepository.delete(project);

        assertTrue(projectRepository.findById(projectId).isEmpty());
    }

    private User createUser() {
        User user = new User(
            "user-" + UUID.randomUUID() + "@example.com", 
            "hashed-password"
        );

        return userRepository.save(user);
    }
}