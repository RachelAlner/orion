package com.orion.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateProjectRequest(
        @NotBlank 
        @Size(max = 255)
        String name, 

        @Size(max = 2000)
        String description, 

        LocalDate deadline, 

        @Min(1)
        @Max(5)
        Integer priority
) {}