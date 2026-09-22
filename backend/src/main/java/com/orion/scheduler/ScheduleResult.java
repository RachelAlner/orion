package com.orion.scheduler;

import java.util.List;

public record ScheduleResult(
        List<ScheduleCandidate> scheduledBlocks,
        List<UnscheduledTask> unscheduledTasks
) {}