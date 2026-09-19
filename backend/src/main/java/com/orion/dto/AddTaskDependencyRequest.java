package com.orion.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AddTaskDependencyRequest(
        @NotNull
        UUID dependsOnTaskId
) {

}