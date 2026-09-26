package com.orion.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ScheduleResponse(
        LocalDateTime periodStart, 
        LocalDateTime periodEnd, 
        List<ScheduleBlockResponse> scheduledBlocks,
        List<UnscheduledTaskResponse> unscheduledTasks 
) {}