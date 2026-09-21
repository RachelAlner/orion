package com.orion.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record CreateAvailabilityRequest(
        @NotBlank 
        DayOfWeek dayOfWeek, 

        @NotBlank
        LocalTime startTime, 

        @NotBlank
        LocalTime endTime
) {}
