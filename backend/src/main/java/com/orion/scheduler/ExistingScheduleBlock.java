package com.orion.scheduler;

import java.time.LocalDateTime;
import java.util.UUID;

public record ExistingScheduleBlock(
        UUID taskId, 
        LocalDateTime startTime, 
        LocalDateTime endTime
) {}