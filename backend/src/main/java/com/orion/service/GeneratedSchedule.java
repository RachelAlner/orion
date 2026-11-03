package com.orion.service;

import com.orion.model.Schedule;
import com.orion.scheduler.ScheduleResult;

public record GeneratedSchedule(
        Schedule schedule,
        ScheduleResult result
) {}