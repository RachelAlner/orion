package com.orion.dto;

import java.util.UUID;
import java.time.DayOfWeek;
import java.time.LocalTime;

public record AvailabilityResponse(
        UUID id, 
        DayOfWeek dayOfWeek, 
        LocalTime startTime, 
        LocalTime endTime
) {}