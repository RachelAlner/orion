package com.orion.controller;

import com.orion.model.Schedule;
import com.orion.dto.ScheduleBlockResponse;
import com.orion.dto.ScheduleResponse;
import com.orion.dto.UnscheduledTaskResponse;
import com.orion.scheduler.ScheduleResult;
import com.orion.service.ScheduleService;
import com.orion.service.GeneratedSchedule;
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
            Authentication authentication
    ) {
        UUID userId = getAuthenticatedUserId(authentication);

        GeneratedSchedule generated =
                scheduleService.generateSchedule(
                        userId
                );
        
        ScheduleResponse response = toScheduleResponse(
                generated.schedule(),
                generated.result()
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

    private ScheduleResponse toScheduleResponse(
            Schedule schedule,
            ScheduleResult result
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
                schedule.getPeriodStart(), 
                schedule.getPeriodEnd(),
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