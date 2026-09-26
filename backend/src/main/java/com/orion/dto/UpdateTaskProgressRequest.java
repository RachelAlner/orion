package com.orion.dto;

import jakarta.validation.constraints.Positive;

public record UpdateTaskProgressRequest(
        @Positive
        int minutesWorked
) {}