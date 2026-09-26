package com.orion.dto;

import com.orion.scheduler.UnscheduledReason;

import java.util.UUID;

public record UnscheduledTaskResponse(
        UUID taskId, 
        int remainingMinutes, 
        UnscheduledReason reason 
) {}