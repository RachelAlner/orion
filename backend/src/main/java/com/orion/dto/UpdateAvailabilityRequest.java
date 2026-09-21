package com.orion.dto;

import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record UpdateAvailabilityRequest(
        @NotNull
        DayOfWeek dayOfWeek, 

        @NotNull
        LocalTime startTime, 

        @NotNull
        LocalTime endTime
) {}