package com.orion.scheduler;

import java.time.LocalDateTime;
import java.util.List;

public record SchedulerInput(
        List<SchedulingTask> tasks, 
        List<TaskDependency> dependencies,
        List<AvailabilityWindow> availabilityWindows,
        List<ExistingScheduleBlock> existingScheduleBlocks,
        LocalDateTime periodStart, 
        LocalDateTime periodEnd
) {}