package com.orion.dto;

import com.orion.model.TaskStatus;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record TaskResponse(
        UUID id, 
        UUID projectId, 
        String title, 
        String description, 
        Integer estimatedMinutes, 
        LocalDate deadline, 
        Integer priority, 
        TaskStatus status, 
        OffsetDateTime createAt, 
        OffsetDateTime updatedAt, 
        OffsetDateTime completedAt
) {}