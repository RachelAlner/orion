package com.orion.controller;

import com.orion.model.Schedule;
import com.orion.dto.GenerateScheduleRequest;
import com.orion.dto.ScheduleBlockResponse;
import com.orion.dto.ScheduleResponse;
import com.orion.dto.UnscheduledTaskResponse;
import com.orion.scheduler.ScheduleResult;
import com.orion.service.ScheduleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.List;

@RestController 
@RequestMapping("/api/schedules")
public class ScheduleController {

    private final ScheduleService scheduleService;

    public ScheduleController(
            ScheduleService scheduleService
    ) {
        this.scheduleService = scheduleService;
    }

    @PostMapping("/generate")
    public ResponseEntity<ScheduleResponse> generateSchedule(
            @RequestBody GenerateScheduleRequest request,
            Authentication authentication
    ) {
        UUID userId = getAuthenticatedUserId(authentication);

        validateRequest(request);

        ScheduleResult result =
                scheduleService.generateSchedule(
                        userId,
                        request.periodStart(),
                        request.periodEnd()
                );
        
        ScheduleResponse response = 
                toScheduleResponse(
                        request, 
                        result
                );
        
        return ResponseEntity.ok(response);
    }

    private UUID getAuthenticatedUserId(
            Authentication authentication
    ) {
        if (authentication == null
                || authentication.getName() == null) {
            throw new IllegalArgumentException(
                    "Authentication is required"
            );
        }

        try {
            return UUID.fromString(
                    authentication.getName()
            );
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Invalid authenticated user ID"
            );
        }
    }

    @GetMapping("/current")
    public ResponseEntity<ScheduleResponse> getCurrentSchedule(
            Authentication authentication
    ) {
        UUID userId = getAuthenticatedUserId(authentication);

        Schedule schedule = 
                scheduleService.getCurrentSchedule(userId);

        ScheduleResponse response = 
                toScheduleResponse(schedule);
        
        return ResponseEntity
                .ok(response);
    }

    private void validateRequest(
            GenerateScheduleRequest request
    ) {
        if (request == null) {
            throw new IllegalArgumentException(
                    "Request body is required"
            );
        }

        if (request.periodStart() == null 
                || request.periodEnd() == null) {
            throw new IllegalArgumentException(
                    "Scheduling period must be provided"
            );
        }

        if (!request.periodStart()
                .isBefore(request.periodEnd())) {
            throw new IllegalArgumentException(
                    "Scheduling period start must be before period end"
            );
        }
    }

    private ScheduleResponse toScheduleResponse(
            GenerateScheduleRequest request, 
            ScheduleResult result
    ) { 
        List<ScheduleBlockResponse> scheduledBlocks = 
                result.scheduledBlocks()
                        .stream()
                        .map(block -> 
                                new ScheduleBlockResponse(
                                        block.taskId(), 
                                        block.startTime(),
                                        block.endTime()
                                )
                        )
                        .toList();
        
        List<UnscheduledTaskResponse>  unscheduledTasks = 
                result.unscheduledTasks()
                        .stream()
                        .map(task -> 
                                new UnscheduledTaskResponse(
                                        task.taskId(), 
                                        task.remainingMinutes(),
                                        task.reason()
                                )
                        )
                        .toList();
        
        return new ScheduleResponse(
                request.periodStart(), 
                request.periodEnd(),
                scheduledBlocks, 
                unscheduledTasks
        );
    }

    private ScheduleResponse toScheduleResponse(
            Schedule schedule
    ) {
        List<ScheduleBlockResponse> scheduledBlocks = 
                schedule.getBlocks()
                        .stream()
                        .map(block -> 
                                new ScheduleBlockResponse(
                                        block.getTask().getId(),
                                        block.getStartTime(),
                                        block.getEndTime()
                                )
                        )
                        .toList();
        
        return new ScheduleResponse(
                schedule.getPeriodStart(),
                schedule.getPeriodEnd(),
                scheduledBlocks,
                List.of()
        );
    }
}