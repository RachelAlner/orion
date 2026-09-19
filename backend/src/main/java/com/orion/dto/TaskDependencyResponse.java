package com.orion.dto;

import java.util.UUID;

public record TaskDependencyResponse(
    UUID taskId, 
    UUID dependsOnTaskId
) {}