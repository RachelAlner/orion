package com.orion.dto;

import java.time.LocalDateTime;

public record GenerateScheduleRequest(
        LocalDateTime periodStart, 
        LocalDateTime periodEnd
) {}