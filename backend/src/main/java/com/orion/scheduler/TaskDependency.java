package com.orion.scheduler;

import java.util.UUID;

public record TaskDependency(
        UUID taskId, 
        UUID dependsOnTaskId
) {}