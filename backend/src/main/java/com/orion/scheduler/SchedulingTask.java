package com.orion.scheduler;

import com.orion.model.TaskStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record SchedulingTask(
        UUID taskId, 
        int estimatedMinutes, 
        int remainingMinutes, 
        LocalDateTime deadline, 
        int priority, 
        TaskStatus status, 
        LocalDateTime createdAt
) {}