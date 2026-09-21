package com.orion.controller;

import com.orion.dto.AvailabilityResponse;
import com.orion.dto.CreateAvailabilityRequest;
import com.orion.dto.UpdateAvailabilityRequest;
import com.orion.exception.AvailabilityNotFoundException;
import com.orion.exception.InvalidAvailabilityException;
import com.orion.exception.GlobalExceptionHandler;
import com.orion.service.AvailabilityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

@WebMvcTest(AvailabilityController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class AvailabilityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean 
    private AvailabilityService availabilityService;

    @MockitoBean
    private Authentication authentication;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        when(authentication.getName())
                .thenReturn(userId.toString());
    }

    @Test 
    void shouldGetAvailability() throws Exception {
        UUID availabilityId = UUID.randomUUID();

        AvailabilityResponse response = 
                new AvailabilityResponse(
                        availabilityId, 
                        DayOfWeek.MONDAY, 
                        LocalTime.of(9, 0),
                        LocalTime.of(12, 0)
                );
        
        when(availabilityService.findAll(userId))
                .thenReturn(List.of(response));

        mockMvc.perform(
                get("/api/availability")
                        .principal(authentication)
        )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        "application/json"
                ))
                .andExpect(jsonPath("$[0].id")
                        .value(availabilityId.toString()))
                .andExpect(jsonPath("$[0].dayOfWeek")
                        .value("MONDAY"))
                .andExpect(jsonPath("$[0].startTime")
                        .value("09:00:00"))
                .andExpect(jsonPath("$[0].endTime")
                        .value("12:00:00"));

        verify(availabilityService).findAll(userId);
    }

    @Test 
    void shouldReturnEmptyListWhenUserHasNoAvailability() throws Exception {
        when(availabilityService.findAll(userId))
                .thenReturn(List.of());
        
        mockMvc.perform(
                get("/api/availability")
                        .principal(authentication)
        )
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
        
        verify(availabilityService).findAll(userId);

    }

    @Test 
    void shouldCreateAvailability() throws Exception {
        UUID availabilityId = UUID.randomUUID();

        AvailabilityResponse response = 
                new AvailabilityResponse(
                        availabilityId, 
                        DayOfWeek.MONDAY, 
                        LocalTime.of(9, 0),
                        LocalTime.of(12, 0)
                );
        
        when(availabilityService.create(
                eq(userId),
                any(CreateAvailabilityRequest.class)
        )).thenReturn(response);
    
        mockMvc.perform(
                post("/api/availability")
                        .principal(authentication)
                        .contentType("application/json")
                        .content("""
                                {
                                    "dayOfWeek": "MONDAY",
                                    "startTime": "09:00:00",
                                    "endTime": "12:00:00"
                                }
                                """)
        )
                .andExpect(status().isCreated())
                .andExpect(header().string(
                        "Location", 
                        "/api/availability/" + availabilityId
                ))
                .andExpect(jsonPath("$.id")
                        .value(availabilityId.toString()))
                .andExpect(jsonPath("$.dayOfWeek")
                        .value("MONDAY"))
                .andExpect(jsonPath("$.startTime")
                        .value("09:00:00"))
                .andExpect(jsonPath("$.endTime")
                        .value("12:00:00"));

        verify(availabilityService).create(
                eq(userId),
                any(CreateAvailabilityRequest.class)
        );

    }

    @Test 
    void shouldRejectInvalidCreateRequest() throws Exception {
        mockMvc.perform(
                post("/api/availability")
                        .principal(authentication)
                        .contentType("application/json")
                        .content("""
                                {
                                    "dayOfWeek": null,
                                    "startTime": null,
                                    "endTime": null
                                }
                                """)
        )
                .andExpect(status().isBadRequest());

        verify(availabilityService, never())
                .create(
                        eq(userId),
                        any(CreateAvailabilityRequest.class)
                );

    }

    @Test 
    void shouldUpdateAvailability() throws Exception {
        UUID availabilityId = UUID.randomUUID();

        AvailabilityResponse response = 
                new AvailabilityResponse(
                        availabilityId, 
                        DayOfWeek.TUESDAY, 
                        LocalTime.of(10, 0),
                        LocalTime.of(13, 0)
                );
        
        when(availabilityService.update(
                eq(userId),
                eq(availabilityId),
                any(UpdateAvailabilityRequest.class)
        )).thenReturn(response);

        mockMvc.perform(
                put("/api/availability/{availabilityId}", availabilityId)
                        .principal(authentication)
                        .contentType("application/json")
                        .content("""
                                {
                                    "dayOfWeek": "TUESDAY",
                                    "startTime": "10:00:00",
                                    "endTime": "13:00:00"
                                }
                                """)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(availabilityId.toString()))
                .andExpect(jsonPath("$.dayOfWeek")
                        .value("TUESDAY"))
                .andExpect(jsonPath("$.startTime")
                        .value("10:00:00"))
                .andExpect(jsonPath("$.endTime")
                        .value("13:00:00"));

        verify(availabilityService).update(
                eq(userId),
                eq(availabilityId),
                any(UpdateAvailabilityRequest.class)
        );

    }

    @Test 
    void shouldReturnNotFoundWhenUpdatingMissingAvailability() throws Exception {
        UUID availabilityId = UUID.randomUUID();

        when(availabilityService.update(
                eq(userId),
                eq(availabilityId),
                any(UpdateAvailabilityRequest.class)
        )).thenThrow(new AvailabilityNotFoundException());

        mockMvc.perform(
                put("/api/availability/{availabilityId}", availabilityId)
                        .principal(authentication)
                        .contentType("application/json")
                        .content("""
                                {
                                    "dayOfWeek": "TUESDAY",
                                    "startTime": "10:00:00",
                                    "endTime": "13:00:00"
                                }
                                """)
        )
                .andExpect(status().isNotFound());
    }

    @Test 
    void shouldReturnConflictWhenUpdatingWithOverlap() throws Exception {
        UUID availabilityId = UUID.randomUUID();

        when(availabilityService.update(
                eq(userId),
                eq(availabilityId),
                any(UpdateAvailabilityRequest.class)
        )).thenThrow(new InvalidAvailabilityException(
                            "Availability periods must not overlap"
        ));

        mockMvc.perform(
                put("/api/availability/{availabilityId}", availabilityId)
                        .principal(authentication)
                        .contentType("application/json")
                        .content("""
                                {
                                    "dayOfWeek": "TUESDAY",
                                    "startTime": "11:00:00",
                                    "endTime": "15:00:00"
                                }
                                """)
        )
                .andExpect(status().isConflict());
    }

    @Test 
    void shouldDeleteAvailability() throws Exception {
        UUID availabilityId = UUID.randomUUID();

        doNothing().when(availabilityService)
                .delete(userId, availabilityId);
        
        mockMvc.perform(
                delete("/api/availability/{availabilityId}", availabilityId)
                        .principal(authentication)
        )
                .andExpect(status().isNoContent());

        verify(availabilityService)
                .delete(userId, availabilityId);
    }

    @Test 
    void shouldReturnNotFoundWhenDeletingMissingAvailability() throws Exception {
        UUID availabilityId = UUID.randomUUID();

        doThrow(new AvailabilityNotFoundException())
                .when(availabilityService)
                .delete(userId, availabilityId);

        mockMvc.perform(
                delete("/api/availability/{availabilityId}", availabilityId)
                        .principal(authentication)
        )
                .andExpect(status().isNotFound());
        
        verify(availabilityService)
                .delete(userId, availabilityId);

    }

    @Test 
    void shouldReturnConflictWhenCreatingOverlappingAvailability() throws Exception {
        when(availabilityService.create(
                eq(userId),
                any(CreateAvailabilityRequest.class)
        )).thenThrow(
                new InvalidAvailabilityException(
                        "Availability periods must not overlap"
                )
        );

        mockMvc.perform(
                post("/api/availability")
                        .principal(authentication)
                        .contentType("application/json")
                        .content("""
                                {
                                    "dayOfWeek": "MONDAY",
                                    "startTime": "11:00:00",
                                    "endTime": "14:00:00"
                                }
                                """)
        )
                .andExpect(status().isConflict());

        verify(availabilityService).create(
                eq(userId),
                any(CreateAvailabilityRequest.class)
        );
    }
}