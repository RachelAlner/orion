package com.orion.controller;

import com.orion.dto.AddTaskDependencyRequest;
import com.orion.model.TaskDependency;
import com.orion.model.TaskDependencyId;
import com.orion.service.TaskDependencyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskDependencyController.class)
@AutoConfigureMockMvc(addFilters = false)
class TaskDependencyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskDependencyService taskDependencyService;

    @MockitoBean 
    private Authentication authentication;

    private UUID projectId;
    private UUID taskId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        projectId = UUID.randomUUID();
        taskId = UUID.randomUUID();

        when(authentication.getName())
                .thenReturn(userId.toString());

    }

    @Test 
    void shouldAddDependency() throws Exception {
        UUID dependsOnTaskId = UUID.randomUUID();

        TaskDependency dependency = new TaskDependency(
                new TaskDependencyId(taskId, dependsOnTaskId)
        );

        when(taskDependencyService.addDependency(userId, projectId, taskId, dependsOnTaskId))
                .thenReturn(dependency);
        
        mockMvc.perform(post(
                "/api/projects/{projectId}/tasks/{taskId}/dependencies",
                projectId, 
                taskId
            )
            .principal(authentication)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                    {
                        "dependsOnTaskId": "%s"
                    }
                    """.formatted(dependsOnTaskId)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.taskId").value(taskId.toString()))
            .andExpect(jsonPath("$.dependsOnTaskId").value(dependsOnTaskId.toString()));

        verify(taskDependencyService)
                .addDependency(userId, projectId, taskId, dependsOnTaskId);
    }

    @Test 
    void shouldRejectDependencyRequestWithoutDependsOnTaskId() throws Exception {
        mockMvc.perform(post(
                "/api/projects/{projectId}/tasks/{taskId}/dependencies",
                projectId, 
                taskId
            )
            .principal(authentication)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                    {}
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        
        verifyNoInteractions(taskDependencyService);

    }

    @Test 
    void shouldRejectInvalidDependsOnTaskId() throws Exception {

        mockMvc.perform(post(
                "/api/projects/{projectId}/tasks/{taskId}/dependencies",
                projectId, 
                taskId
            )
            .principal(authentication)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                    {
                        "dependsOnTaskId": "not_a_uuid"
                    }
                    """))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(taskDependencyService);
    }

    @Test 
    void shouldGetDependencies() throws Exception {
        UUID dependency1 = UUID.randomUUID();
        UUID dependency2 = UUID.randomUUID();

        TaskDependency first = new TaskDependency(
                new TaskDependencyId(taskId, dependency1)
        );

        TaskDependency second = new TaskDependency(
                new TaskDependencyId(taskId, dependency2)
        );

        when(taskDependencyService.findDependencies(userId, projectId, taskId))
                .thenReturn(List.of(first, second));
        
        mockMvc.perform(get(
                "/api/projects/{projectId}/tasks/{taskId}/dependencies",
                projectId, 
                taskId
            ).principal(authentication)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].taskId").value(taskId.toString()))
            .andExpect(jsonPath("$[0].dependsOnTaskId").value(dependency1.toString()))
            .andExpect(jsonPath("$[1].taskId").value(taskId.toString()))
            .andExpect(jsonPath("$[1].dependsOnTaskId").value(dependency2.toString()));

        verify(taskDependencyService)
                .findDependencies(userId, projectId, taskId);

    }

    @Test 
    void shouldReturnEmptyListWhenTaskHasNoDependencies() throws Exception {
        when(taskDependencyService.findDependencies(userId, projectId, taskId))
                .thenReturn(List.of());
        
        mockMvc.perform(get(
                "/api/projects/{projectId}/tasks/{taskId}/dependencies",
                projectId, 
                taskId
            ).principal(authentication)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));

        verify(taskDependencyService)
                .findDependencies(userId, projectId, taskId);
    }

    @Test 
    void shouldRemoveDependency() throws Exception {
        UUID dependsOnTaskId = UUID.randomUUID();

        doNothing()
                .when(taskDependencyService)
                .removeDependency(userId, projectId, taskId, dependsOnTaskId);
        
        mockMvc.perform(delete(
                "/api/projects/{projectId}/tasks/{taskId}/dependencies/{dependsOnTaskId}",
                projectId, 
                taskId, 
                dependsOnTaskId
            ).principal(authentication)
            )
            .andExpect(status().isNoContent());

        verify(taskDependencyService)
                .removeDependency(userId, projectId, taskId, dependsOnTaskId);
    }
}