package com.orion.controller;

import com.orion.model.Project;
import com.orion.model.ProjectStatus;
import com.orion.service.ProjectService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(ProjectController.class)
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProjectService projectService;

    private final UUID userId = 
            UUID.fromString(
                "11111111-1111-1111-1111-111111111111"  
            );

    private final UUID projectId = 
            UUID.fromString(
                "22222222-2222-2222-2222-222222222222"
            );
    
    private Project createProject() {
        return new Project(
                userId, 
                "Orion", 
                "Scheduling app", 
                LocalDateTime.of(2027, 6, 1, 0, 0), 
                5
        );
    }

    @Test 
    @WithMockUser(username = "11111111-1111-1111-1111-111111111111")
    void shouldGetAllProjectsForAuthenticatedUser() throws Exception {
        Project project = createProject();

        when(projectService.findAllForUser(userId)).thenReturn(List.of(project));

        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Orion"))
                .andExpect(jsonPath("$[0].description").value("Scheduling app"))
                .andExpect(jsonPath("$[0].priority").value(5))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));
        
        verify(projectService).findAllForUser(userId);
    }

    @Test
    @WithMockUser(username = "11111111-1111-1111-1111-111111111111")
    void shouldGetProjectById() throws Exception {
        Project project = createProject();

        when(projectService.findByIdForUser(projectId, userId)).thenReturn(project);

        mockMvc.perform(get("/api/projects/{projectId}", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Orion"))
                .andExpect(jsonPath("$.priority").value(5))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
        
        verify(projectService).findByIdForUser(projectId, userId);
    }

    @Test 
    @WithMockUser(username = "11111111-1111-1111-1111-111111111111")
    void shouldCreateProject() throws Exception {
        Project project = createProject();

        when(projectService.createProject(
                eq(userId), 
                eq("Orion"),
                eq("Scheduling app"),
                eq(LocalDateTime.of(2027, 6, 1, 0, 0)),
                eq(5)
        )).thenReturn(project);

        String request = """
                {
                    "name": "Orion", 
                    "description": "Scheduling app",
                    "deadline": "2027-06-01T00:00:00", 
                    "priority": 5
                }
                """;
        
        mockMvc.perform(
                post("/api/projects")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request)
        )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Orion"))
                .andExpect(jsonPath("$.description").value("Scheduling app"))
                .andExpect(jsonPath("$.priority").value(5))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
        
        verify(projectService)
                .createProject(
                        userId, 
                        "Orion", 
                        "Scheduling app", 
                        LocalDateTime.of(2027, 6, 1, 0, 0),
                        5
                );

    }

    @Test 
    @WithMockUser(username = "11111111-1111-1111-1111-111111111111")
    void shouldRejectInvalidCreateRequest() throws Exception {
        String request = """
                {
                    "name": "",
                    "description": "Invalid project",
                    "deadline": "2027-06-01",
                    "priority": 5
                }
                """;
        mockMvc.perform(
                post("/api/projects")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request)
        )
                .andExpect(status().isBadRequest());
        
        verifyNoInteractions(projectService);
    }

    @Test 
    @WithMockUser(username = "11111111-1111-1111-1111-111111111111")
    void shouldUpdateProject() throws Exception {
        Project project = createProject();

        when(projectService.updateProject(
                eq(projectId), 
                eq(userId), 
                eq("Updated Orion"), 
                eq("Updated description"),
                eq(LocalDateTime.of(2027, 7, 1, 0, 0)), 
                eq(4)
        )).thenReturn(project);

        String request = """
                {
                    "name": "Updated Orion", 
                    "description": "Updated description", 
                    "deadline": "2027-07-01T00:00:00",
                    "priority": 4
                }
                """;
        
        mockMvc.perform(
                put("/api/projects/{projectId}", projectId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request)
        )
                .andExpect(status().isOk());
        
        verify(projectService)
                .updateProject(
                        projectId, 
                        userId, 
                        "Updated Orion", 
                        "Updated description", 
                        LocalDateTime.of(2027, 7, 1, 0, 0), 
                        4
                );
    }

    @Test 
    @WithMockUser(username = "11111111-1111-1111-1111-111111111111")
    void shouldDeleteProject() throws Exception {

        doNothing().when(projectService).deleteProject(projectId, userId);

        mockMvc.perform(
                delete("/api/projects/{projectId}", projectId)
                        .with(csrf())
        )
                .andExpect(status().isNoContent());
        
        verify(projectService).deleteProject(projectId, userId);
    }
}
