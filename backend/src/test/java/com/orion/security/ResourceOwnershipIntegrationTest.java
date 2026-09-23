package com.orion.security;

import com.orion.dto.AuthResponse;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ResourceOwnershipIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String userAToken;
    private String userBToken;

    private UUID projectAId;
    private UUID taskAId;

    private String userAEmail;
    private String userBEmail;

    private UUID projectBId;
    private UUID taskBId;


    @BeforeEach
    void setUp() throws Exception{
        userAEmail = "ownership-a-" + UUID.randomUUID() + "@test.com";
        userBEmail = "ownership-b-" + UUID.randomUUID() + "@test.com";
        
        userAToken = registerAndLogin(userAEmail);
        userBToken = registerAndLogin(userBEmail);

        projectAId = createProject(
                userAToken, 
                "User A Project"
        );

        taskAId = createTask(
                userAToken, 
                projectAId, 
                "User A Task"
        );

        projectBId = createProject(
                userBToken, 
                "User B Project"
        );

        taskBId = createTask(
                userBToken, 
                projectBId, 
                "User B Task"
        );
    }

    @Test 
    void userCanAccessOwnProject() throws Exception {
        mockMvc.perform(get("/api/projects/{projectId}", projectAId)
                        .header("Authorization", bearer(userAToken)))
                .andExpect(status().isOk());
    }

    @Test 
    void userCannotAccessAnotherUsersProject() throws Exception {
        mockMvc.perform(get("/api/projects/{projectId}", projectBId)
                        .header("Authorization", bearer(userAToken)))
                .andExpect(status().isNotFound());
    }

    @Test 
    void userCannotUpdateAnotherUsersProject() throws Exception {
        mockMvc.perform(put("/api/projects/{projectId}", projectBId)
                        .header("Authorization", bearer(userAToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Hijacked project",
                                    "description": "Should not be allowed", 
                                    "deadline": "2027-05-01T00:00:00", 
                                    "priority": 1
                                }
                                """
                        ))
                .andExpect(status().isNotFound());
    }

    @Test 
    void userCannotDeleteAnotherUsersProject() throws Exception {
        mockMvc.perform(delete("/api/projects/{projectId}", projectBId)
                        .header("Authorization", bearer(userAToken)))
                .andExpect(status().isNotFound());
    }

    @Test 
    void userCanAccessOwnTask() throws Exception {
        mockMvc.perform(get(
                                "/api/projects/{projectId}/tasks/{taskId}", 
                                projectAId, 
                                taskAId
                        )
                        .header("Authorization", bearer(userAToken)))
                .andExpect(status().isOk());
    }

    @Test 
    void userCannotAccessAnotherUsersTask() throws Exception {
        mockMvc.perform(get(
                                "/api/projects/{projectId}/tasks/{taskId}", 
                                projectBId, 
                                taskBId
                        )
                        .header("Authorization", bearer(userAToken)))
                .andExpect(status().isNotFound());
    }

    @Test 
    void userCannotUpdateAnotherUsersTask() throws Exception {
        mockMvc.perform(put(
                                "/api/projects/{projectId}/tasks/{taskId}", 
                                projectBId, 
                                taskBId
                        )
                        .header("Authorization", bearer(userAToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "Hijacked task",
                                    "description": "Should not be allowed", 
                                    "estimatedMinutes": 60,
                                    "deadline": "2027-05-01T00:00:00", 
                                    "priority": 1
                                }
                                """
                        ))
                .andExpect(status().isNotFound());
    }

    @Test 
    void userCannotDeleteAnotherUsersTask() throws Exception {
        mockMvc.perform(delete(
                                "/api/projects/{projectId}/tasks/{taskId}", 
                                projectBId, 
                                taskBId
                        )
                        .header("Authorization", bearer(userAToken)))
                .andExpect(status().isNotFound());
    }

    @Test 
    void unauthenticatedRequestCannotAccessProject() throws Exception {
        mockMvc.perform(get("/api/projects/{projectId}", projectAId))
                .andExpect(status().isUnauthorized());
    }

    @Test 
    void unauthenticatedRequestCannotAccessTask() throws Exception {
        mockMvc.perform(get(
                                "/api/projects/{projectId}/tasks/{taskId}", 
                                projectAId, 
                                taskAId
                        ))
                .andExpect(status().isUnauthorized());
    }

    @Test 
    void userCannotAddDependencyToAnotherUsersTask() throws Exception {
        mockMvc.perform(post(
                                "/api/projects/{projectId}/tasks/{taskId}/dependencies", 
                                projectBId, 
                                taskBId
                        )
                        .header("Authorization", bearer(userAToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "dependsOnTaskId": "%s"
                                }
                                """.formatted(taskAId)
                        ))
                .andExpect(status().isNotFound());
    }

    @Test 
    void userCannotRemoveDependencyFromAnotherUsersTask() throws Exception {
        mockMvc.perform(delete(
                                "/api/projects/{projectId}/tasks/{taskId}/dependencies/{dependsOnTaskId}", 
                                projectBId, 
                                taskBId, 
                                taskAId
                        )
                        .header("Authorization", bearer(userAToken))
                        )
                .andExpect(status().isNotFound());
    }

    private String registerAndLogin(String email) throws Exception {
        mockMvc.perform(
                post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "%s",
                                    "password": "Password123!"
                                }
                                """.formatted(email))
                )
                .andExpect(status().isCreated());
            
        String response = mockMvc.perform(
                post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "%s",
                                    "password": "Password123!"
                                }
                                """.formatted(email))
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        
        AuthResponse authResponse = 
                objectMapper.readValue(response, AuthResponse.class);

        return authResponse.token();

    }
    private UUID createProject(
            String token, 
            String name
    ) throws Exception {

        String response = mockMvc.perform(post("/api/projects")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "%s", 
                                    "description": "Integration test project", 
                                    "deadline": "2027-05-01T00:00:00", 
                                    "priority": 3
                                }
                                """.formatted(name)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(response);
        
        return UUID.fromString(
                json.get("id").asString()
        );
    }

    private UUID createTask(
            String token, 
            UUID projectId, 
            String title
    ) throws Exception {

        String response = mockMvc.perform(post("/api/projects/{projectId}/tasks", projectId)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "%s", 
                                    "description": "Integration test task", 
                                    "estimatedMinutes": 60, 
                                    "deadline": "2027-05-01T00:00:00", 
                                    "priority": 3
                                }
                                """.formatted(title)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        
        JsonNode json = objectMapper.readTree(response);
        
        return UUID.fromString(
                json.get("id").asString()
        );
    }


    private String bearer(String token) {
        return "Bearer " + token;
    }
}