package com.orion.scheduler;

import java.time.LocalDateTime;

public record AvailabilityWindow(
        LocalDateTime startTime, 
        LocalDateTime endTime
) {}