package com.orion.dto;

import jakarta.validation.constraints.Min;

public record UpdateTaskProgressRequest(
        @Min(0)
        Integer workedMinutes
) {}