package com.orion.scheduler;

import java.util.UUID;

public record UnscheduledTask(
        UUID taskId,
        int remainingMinutes,
        UnscheduledReason reason 
) {}