package com.orion.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record UpdateProjectRequest(
        @NotBlank 
        @Size(max = 200)
        String name, 

        @Size(max = 2000)
        String description, 

        LocalDateTime deadline, 

        @Min(1)
        @Max(5)
        Integer priority
) {}