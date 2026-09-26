package com.orion.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ScheduleBlockResponse(
        UUID taskId, 
        LocalDateTime startTime, 
        LocalDateTime endTime
) {}