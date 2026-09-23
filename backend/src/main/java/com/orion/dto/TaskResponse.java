package com.orion.dto;

import com.orion.model.TaskStatus;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;

public record TaskResponse(
        UUID id, 
        UUID projectId, 
        String title, 
        String description, 
        Integer estimatedMinutes, 
        LocalDateTime deadline, 
        Integer priority, 
        TaskStatus status, 
        OffsetDateTime createdAt, 
        OffsetDateTime updatedAt, 
        OffsetDateTime completedAt
) {}