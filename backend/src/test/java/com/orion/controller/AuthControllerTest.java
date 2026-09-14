package com.orion.controller;

import com.orion.dto.AuthResponse;
import com.orion.exception.DuplicateEmailException;
import com.orion.exception.GlobalExceptionHandler;
import com.orion.exception.InvalidCredentialsException;
import com.orion.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class AuthControllerTest {

    // authenticate
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean 
    private AuthService authService;

    @Test 
    void shouldRegisterUser() throws Exception {
        UUID userId = UUID.randomUUID();

        AuthResponse response = new AuthResponse(userId, "test@example.com", "jwt-token");

        when(authService.register("test@example.com", "password123")).thenReturn(response);

        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "email": "test@example.com",
                        "password": "password123"
                    }
                """)
        )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.userId").value(userId.toString()))
            .andExpect(jsonPath("$.email").value("test@example.com"))
            .andExpect(jsonPath("$.token").value("jwt-token"));

        verify(authService).register("test@example.com", "password123");
    }

    @Test 
    void shouldLoginUser() throws Exception {
        UUID userId = UUID.randomUUID();

        AuthResponse response = new AuthResponse(userId, "test@example.com", "jwt-token");

        when(authService.login("test@example.com", "password123")).thenReturn(response);

        mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "email": "test@example.com",
                        "password": "password123"
                    }
                """)
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userId").value(userId.toString()))
            .andExpect(jsonPath("$.email").value("test@example.com"))
            .andExpect(jsonPath("$.token").value("jwt-token"));

        verify(authService).login("test@example.com", "password123");
    }

    @Test 
    void shouldRejectRegistrationWithInvalidEmail() throws Exception {
        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "email": "not-an-email",
                        "password": "password123"
                    }
                """)
        )
            .andExpect(status().isBadRequest());
    }

    @Test 
    void shouldRejectRegistrationWithBlankEmail() throws Exception {
        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "email": "",
                        "password": "password123"
                    }
                """)
        )
            .andExpect(status().isBadRequest());
    }

    @Test 
    void shouldRejectRegistrationWithShortPassword() throws Exception {
        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "email": "test@example.com",
                        "password": "123"
                    }
                """)
        )
            .andExpect(status().isBadRequest());
    }

    @Test 
    void shouldRejectRegistrationWithBlankPassword() throws Exception {
        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "email": "test@example.com",
                        "password": ""
                    }
                """)
        )
            .andExpect(status().isBadRequest());
    }

    @Test 
    void shouldRejectLoginWithInvalidEmail() throws Exception {
        mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "email": "not-an-email",
                        "password": "password123"
                    }
                """)
        )
            .andExpect(status().isBadRequest());
    }

    @Test 
    void shouldRejectLoginWithBlankPassword() throws Exception {
        mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "email": "test@example.com",
                        "password": ""
                    }
                """)
        )
            .andExpect(status().isBadRequest());
    }

    @Test 
    void shouldReturnConflictWhenEmailAlreadyExists() throws Exception {
        when(authService.register("test@example.com","password123")).thenThrow(new DuplicateEmailException("An account with this email already exists."));

        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "email": "test@example.com",
                        "password": "password123"
                    }
                """)
        )
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("DUPLICATE_EMAIL"))
            .andExpect(jsonPath("$.message").value("An account with this email already exists."));

        verify(authService).register("test@example.com", "password123");

    }

    @Test 
    void shouldReturnUnauthorisedWhenCredentialsAreInvalid() throws Exception {
        when(authService.login("test@example.com","wrong-password")).thenThrow(new InvalidCredentialsException("Invalid email or password."));

        mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "email": "test@example.com",
                        "password": "wrong-password"
                    }
                """)
        )
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"))
            .andExpect(jsonPath("$.message").value("Invalid email or password."));

        verify(authService).login("test@example.com", "wrong-password");

    }


}
