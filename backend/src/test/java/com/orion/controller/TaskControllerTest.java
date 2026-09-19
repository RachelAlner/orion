package com.orion.controller;

import com.orion.dto.CreateTaskRequest;
import com.orion.dto.UpdateTaskRequest;
import com.orion.exception.TaskNotFoundException;
import com.orion.model.Task;
import com.orion.model.TaskStatus;
import com.orion.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.core.Authentication;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
@AutoConfigureMockMvc(addFilters = false)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    @MockitoBean
    private Authentication authentication;

    private UUID userId;
    private UUID projectId;
    private UUID taskId;


    private Task task;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        projectId = UUID.randomUUID();
        taskId = UUID.randomUUID();

        when(authentication.getName())
                .thenReturn(userId.toString());


        task = new Task(
                projectId, 
                "Implement scheduler", 
                "Implement algorithm", 
                120, 
                LocalDate.of(2027, 5, 1), 
                3
        );
    }

    @Test 
    void shouldGetTasks() throws Exception {
        when(authentication.getName())
                .thenReturn(userId.toString());

        Task task2 = new Task(
                projectId, 
                "Write tests", 
                "Writer scheduler tests", 
                60, 
                LocalDate.of(2027, 5, 1), 
                4
        );

        when(taskService.findAllForProject(userId, projectId))
                .thenReturn(List.of(task, task2));

        mockMvc.perform(
                get("/api/projects/{projectId}/tasks", projectId)
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("Implement scheduler"))
                .andExpect(jsonPath("$[0].projectId").value(projectId.toString()))
                .andExpect(jsonPath("$[1].title").value("Write tests"));

        verify(taskService).findAllForProject(userId, projectId);
    }

    @Test 
    void shouldReturnEmptyListWhenProjectHasNoTasks() throws Exception {
        when(authentication.getName())
                .thenReturn(userId.toString());

        when(taskService.findAllForProject(userId, projectId)).thenReturn(List.of());

        mockMvc.perform(
                get("/api/projects/{projectId}/tasks", projectId)
                        .principal(authentication)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
        
        verify(taskService).findAllForProject(userId, projectId);

    }

    @Test 
    void shouldGetTask() throws Exception {
        when(authentication.getName())
                .thenReturn(userId.toString());

        when(taskService.findByIdForProject(
                userId, 
                taskId, 
                projectId
        )).thenReturn(task);

        mockMvc.perform(
                get("/api/projects/{projectId}/tasks/{taskId}", projectId, taskId)
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(task.getId().toString()))
                .andExpect(jsonPath("$.projectId").value(projectId.toString()))
                .andExpect(jsonPath("$.title").value("Implement scheduler"))
                .andExpect(jsonPath("$.description").value("Implement algorithm"))
                .andExpect(jsonPath("$.estimatedMinutes").value(120))
                .andExpect(jsonPath("$.priority").value(3))
                .andExpect(jsonPath("$.status").value("TODO"));
        
        verify(taskService).findByIdForProject(
                    userId, 
                    taskId, 
                    projectId
        );

    }

    @Test 
    void shouldCreateTask() throws Exception {
        when(authentication.getName())
                .thenReturn(userId.toString());

        CreateTaskRequest request = 
                new CreateTaskRequest(
                        "Implement scheduler", 
                        "Implement algorithm", 
                        120, 
                        LocalDate.of(2027, 5, 1), 
                        3
                );
        
        when(taskService.createTask(
                eq(userId),
                eq(projectId), 
                eq("Implement scheduler"), 
                eq("Implement algorithm"), 
                eq(120), 
                eq(LocalDate.of(2027, 5, 1)),
                eq(3)
        )).thenReturn(task);

        mockMvc.perform(
                post("/api/projects/{projectId}/tasks", projectId)
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                        "title": "Implement scheduler",
                        "description": "Implement algorithm",
                        "estimatedMinutes": 120,
                        "deadline": "2027-05-01",
                        "priority": 3
                    }
                """)
        )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(task.getId().toString()))
                .andExpect(jsonPath("$.projectId").value(projectId.toString()))
                .andExpect(jsonPath("$.title").value("Implement scheduler"))
                .andExpect(jsonPath("$.description").value("Implement algorithm"))
                .andExpect(jsonPath("$.estimatedMinutes").value(120))
                .andExpect(jsonPath("$.priority").value(3))
                .andExpect(jsonPath("$.status").value("TODO"));

        verify(taskService).createTask(
                userId, 
                projectId, 
                "Implement scheduler", 
                "Implement algorithm", 
                120, 
                LocalDate.of(2027, 5, 1), 
                3
        );

    }

    @Test 
    void shouldUpdateTask() throws Exception {
        when(authentication.getName())
                .thenReturn(userId.toString());

        UpdateTaskRequest request = 
                new UpdateTaskRequest(
                        "Updated scheduler", 
                        "Updated description", 
                        180, 
                        LocalDate.of(2027, 6, 1), 
                        5
                );

        Task updatedTask = new Task(
                projectId, 
                "Updated scheduler", 
                "Updated description", 
                180, 
                LocalDate.of(2027, 6, 1), 
                5
        );

        when(taskService.updateTask(
                eq(userId),
                eq(taskId), 
                eq(projectId), 
                eq("Updated scheduler"), 
                eq("Updated description"), 
                eq(180), 
                eq(LocalDate.of(2027, 6, 1)), 
                eq(5)
        )).thenReturn(updatedTask);

        
        mockMvc.perform(
                put("/api/projects/{projectId}/tasks/{taskId}", projectId, taskId)
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                        "title": "Updated scheduler",
                        "description": "Updated description",
                        "estimatedMinutes": 180,
                        "deadline": "2027-06-01",
                        "priority": 5
                    }
                """)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated scheduler"))
                .andExpect(jsonPath("$.description").value("Updated description"))
                .andExpect(jsonPath("$.estimatedMinutes").value(180))
                .andExpect(jsonPath("$.priority").value(5));
        
        verify(taskService).updateTask(
                userId, 
                taskId, 
                projectId, 
                "Updated scheduler", 
                "Updated description", 
                180, 
                LocalDate.of(2027, 6, 1), 
                5
        );

    }

    @Test 
    void shouldCompleteTask() throws Exception {
        when(authentication.getName())
                .thenReturn(userId.toString());

        Task completedTask = new Task(
                projectId, 
                "Complete this task",
                "Test description", 
                60, 
                LocalDate.of(2027, 5, 1), 
                3 
        );

        completedTask.complete();

        when(taskService.completeTask(
                userId, 
                taskId, 
                projectId
        )).thenReturn(completedTask);

        mockMvc.perform(
                post("/api/projects/{projectId}/tasks/{taskId}/complete", projectId, taskId)
                        .principal(authentication)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.completedAt").exists());

        verify(taskService)
                .completeTask(
                        userId, 
                        taskId, 
                        projectId
                );
    }

    @Test 
    void shouldDeleteTask() throws Exception {
        when(authentication.getName())
                .thenReturn(userId.toString());

        doNothing()
                .when(taskService)
                .deleteTask(
                        userId, 
                        taskId, 
                        projectId
                );

        mockMvc.perform(
                delete("/api/projects/{projectId}/tasks/{taskId}", projectId, taskId)
                        .principal(authentication)
        )
                .andExpect(status().isNoContent());

        verify(taskService)
                .deleteTask(
                        userId, 
                        taskId, 
                        projectId
                );
    }

    @Test 
    void shouldRejectInvalidCreateRequest() throws Exception {
        when(authentication.getName())
                .thenReturn(userId.toString());

        CreateTaskRequest request = 
                new CreateTaskRequest(
                        "", 
                        "Invalid task", 
                        0, 
                        LocalDate.of(2027, 5, 1), 
                        6
                );
        
        mockMvc.perform(
                post("/api/projects/{projectId}/tasks", projectId)
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                        "title": "",
                        "description": "Invalid task",
                        "estimatedMinutes": 0,
                        "deadline": "2027-05-01",
                        "priority": 6
                    }
                """)
        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        verify(taskService, never())
                .createTask(
                        any(UUID.class),
                        any(UUID.class), 
                        anyString(), 
                        anyString(), 
                        anyInt(), 
                        any(LocalDate.class), 
                        anyInt()
                );

    }

    @Test 
    void shouldRejectInvalidUpdateRequest() throws Exception {
        when(authentication.getName())
                .thenReturn(userId.toString());

        UpdateTaskRequest request = 
                new UpdateTaskRequest(
                        "", 
                        "Invalid task", 
                        0, 
                        LocalDate.of(2027, 5, 1), 
                        6
                );

        mockMvc.perform(
                put("/api/projects/{projectId}/tasks/{taskId}", projectId, taskId)
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                        "title": "",
                        "description": "Invalid task",
                        "estimatedMinutes": 0,
                        "deadline": "2027-05-01",
                        "priority": 6
                    }
                """)
        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        verify(taskService, never())
                .updateTask(
                        any(UUID.class),
                        any(UUID.class), 
                        any(UUID.class),
                        anyString(), 
                        anyString(), 
                        anyInt(), 
                        any(LocalDate.class), 
                        anyInt()
                );
    }

    @Test 
    void shouldReturnNotFoundWhenTaskDoesNotExist() throws Exception {
        when(authentication.getName())
                .thenReturn(userId.toString());

        when(taskService.findByIdForProject(
                userId, 
                taskId, 
                projectId
        )).thenThrow(
                new TaskNotFoundException(
                        "Task not found."
                )
        );

        mockMvc.perform(
                get("/api/projects/{projectId}/tasks/{taskId}", projectId, taskId)
                        .principal(authentication)
        )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("TASK_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Task not found."));

        verify(taskService)
                .findByIdForProject(
                        userId, 
                        taskId, 
                        projectId
                );
    }
}