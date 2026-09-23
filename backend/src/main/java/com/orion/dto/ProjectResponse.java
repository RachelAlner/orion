package com.orion.dto; 

import com.orion.model.ProjectStatus;

import java.util.UUID;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

public record ProjectResponse(
    UUID id, 
    String name, 
    String description, 
    LocalDateTime deadline, 
    Integer priority, 
    ProjectStatus status, 
    OffsetDateTime createdAt, 
    OffsetDateTime updatedAt
) {

}