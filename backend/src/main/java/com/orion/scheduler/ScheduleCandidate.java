package com.orion.scheduler;

import java.time.LocalDateTime;
import java.util.UUID;

public record ScheduleCandidate(
        UUID taskId, 
        LocalDateTime startTime, 
        LocalDateTime endTime
) {}