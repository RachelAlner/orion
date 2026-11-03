package com.orion.controller;

import com.orion.model.Schedule;
import com.orion.model.ScheduleBlock;
import com.orion.model.Task;
import com.orion.scheduler.ScheduleResult;
import com.orion.scheduler.UnscheduledReason;
import com.orion.scheduler.UnscheduledTask;
import com.orion.service.ScheduleService;
import com.orion.service.GeneratedSchedule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ScheduleController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(com.orion.exception.GlobalExceptionHandler.class)
public class ScheduleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ScheduleService scheduleService;

    @MockitoBean
    private Authentication authentication;

    private UUID userId;
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;

    @BeforeEach
    void setUp() throws Exception {
        userId = UUID.randomUUID();

        periodStart = LocalDateTime.of(2026, 9, 24, 9, 0);
        periodEnd = LocalDateTime.of(2026, 9, 24, 17, 0);

        when(authentication.getName())
                .thenReturn(userId.toString());
    }

    @Test 
    void generatesScheduleSuccessfully() throws Exception {
        UUID taskId = UUID.randomUUID();

        LocalDateTime blockStart = 
                LocalDateTime.of(2026, 9, 24, 10, 0);

        LocalDateTime blockEnd = 
                LocalDateTime.of(2026, 9, 24, 11, 0);
        
        ScheduleResult result = new ScheduleResult(
                List.of(), 
                List.of()
        );

        Task task = mock(Task.class);
        when(task.getId()).thenReturn(taskId);

        ScheduleBlock block = mock(ScheduleBlock.class);
        when(block.getTask()).thenReturn(task);
        when(block.getStartTime()).thenReturn(blockStart);
        when(block.getEndTime()).thenReturn(blockEnd);

        Schedule schedule = mock(Schedule.class);
        when(schedule.getPeriodStart()).thenReturn(periodStart);
        when(schedule.getPeriodEnd()).thenReturn(periodEnd);
        when(schedule.getBlocks()).thenReturn(List.of(block));

        GeneratedSchedule generated =
                new GeneratedSchedule(schedule, result);

        when(scheduleService.generateSchedule(userId))
                .thenReturn(generated);

        mockMvc.perform(
                post("/api/schedules/generate")
                        .principal(authentication)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.periodStart")
                        .value("2026-09-24T09:00:00")
                )
                .andExpect(jsonPath("$.periodEnd")
                        .value("2026-09-24T17:00:00")
                )
                .andExpect(jsonPath("$.scheduledBlocks.length()")
                        .value(1)
                )
                .andExpect(jsonPath("$.scheduledBlocks[0].taskId")
                        .value(taskId.toString())
                )
                .andExpect(jsonPath("$.scheduledBlocks[0].startTime")
                        .value("2026-09-24T10:00:00")
                )
                .andExpect(jsonPath("$.scheduledBlocks[0].endTime")
                        .value("2026-09-24T11:00:00")
                )
                .andExpect(jsonPath("$.unscheduledTasks.length()")
                        .value(0)
                );
    
        verify(scheduleService).generateSchedule(
                userId
        );

    }

    @Test 
    void mapsUnscheduledTasksCorrectly() throws Exception{
        UUID taskId = UUID.randomUUID();

        ScheduleResult result = new ScheduleResult(
                List.of(),
                List.of(
                        new UnscheduledTask(
                                taskId, 
                                120, 
                                UnscheduledReason.INSUFFICIENT_AVAILABILITY
                        )
                )
        );

        Schedule schedule = mock(Schedule.class);

        when(schedule.getPeriodStart()).thenReturn(periodStart);
        when(schedule.getPeriodEnd()).thenReturn(periodEnd);
        when(schedule.getBlocks()).thenReturn(List.of());

        GeneratedSchedule generated =
                new GeneratedSchedule(schedule, result);

        when(scheduleService.generateSchedule(userId))
                .thenReturn(generated);


        mockMvc.perform(
                post("/api/schedules/generate")
                        .principal(authentication)
                        
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scheduledBlocks.length()")
                        .value(0)
                )
                .andExpect(jsonPath("$.unscheduledTasks.length()")
                        .value(1)
                )
                .andExpect(jsonPath("$.unscheduledTasks[0].taskId")
                        .value(taskId.toString())
                )
                .andExpect(jsonPath("$.unscheduledTasks[0].remainingMinutes")
                        .value(120)
                )
                .andExpect(jsonPath("$.unscheduledTasks[0].reason")
                        .value("INSUFFICIENT_AVAILABILITY")
                );

        verify(scheduleService).generateSchedule(userId);
    }

    @Test
    void passesAuthenticatedUserToService() throws Exception {
        Schedule schedule = mock(Schedule.class);

        when(schedule.getPeriodStart()).thenReturn(periodStart);
        when(schedule.getPeriodEnd()).thenReturn(periodEnd);
        when(schedule.getBlocks()).thenReturn(List.of());

        ScheduleResult result = new ScheduleResult(
                List.of(),
                List.of()
        );

        GeneratedSchedule generated =
                new GeneratedSchedule(schedule, result);

        when(scheduleService.generateSchedule(userId))
                .thenReturn(generated);

        mockMvc.perform(
                post("/api/schedules/generate")
                        .principal(authentication)
        )
                .andExpect(status().isOk());

        verify(scheduleService).generateSchedule(userId);
    }

    @Test 
    void rejectsInvalidAuthenticatedUserId() throws Exception {
        when(authentication.getName())
                .thenReturn("not-a-uuid");

        mockMvc.perform(
                post("/api/schedules/generate")
                        .principal(authentication)
                        
        )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(scheduleService);
    }

    @Test 
    void rejectsMissingAuthentication() throws Exception {
        mockMvc.perform(
                post("/api/schedules/generate")
                        
        )
                .andExpect(status().isBadRequest());
        
        verifyNoInteractions(scheduleService);
    }

    @Test 
    void getCurrentScheduleSuccessfully() throws Exception {
        UUID taskId = UUID.randomUUID();

        Task task = mock(Task.class);
        when(task.getId()).thenReturn(taskId);

        LocalDateTime blockStart = 
                LocalDateTime.of(2026, 9, 24, 10, 0);

        LocalDateTime blockEnd = 
                LocalDateTime.of(2026, 9, 24, 11, 0);
        
        ScheduleBlock block = mock(ScheduleBlock.class);

        when(block.getTask()).thenReturn(task);
        when(block.getStartTime()).thenReturn(blockStart);
        when(block.getEndTime()).thenReturn(blockEnd);

        Schedule schedule = mock(Schedule.class);

        when(schedule.getPeriodStart())
                .thenReturn(periodStart);

        when(schedule.getPeriodEnd())
                .thenReturn(periodEnd);

        when(schedule.getBlocks())
                .thenReturn(List.of(block));
        
        when(scheduleService.getCurrentSchedule(userId))
                .thenReturn(schedule);
        
        mockMvc.perform(
                get("/api/schedules/current")
                        .principal(authentication)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.periodStart")
                        .value("2026-09-24T09:00:00")
                )
                .andExpect(jsonPath("$.periodEnd")
                        .value("2026-09-24T17:00:00")
                )
                .andExpect(jsonPath("$.scheduledBlocks.length()")
                        .value(1)
                )
                .andExpect(jsonPath("$.scheduledBlocks[0].taskId")
                        .value(taskId.toString())
                )
                .andExpect(jsonPath("$.scheduledBlocks[0].startTime")
                        .value("2026-09-24T10:00:00")
                )
                .andExpect(jsonPath("$.scheduledBlocks[0].endTime")
                        .value("2026-09-24T11:00:00")
                )
                .andExpect(jsonPath("$.unscheduledTasks.length()")
                        .value(0)
                );

        verify(scheduleService)
                .getCurrentSchedule(userId);
    }

    @Test 
    void mapsMultipleScheduleBlocks() throws Exception {
        UUID firstTaskId = UUID.randomUUID();
        UUID secondTaskId = UUID.randomUUID();

        Task firstTask = mock(Task.class);
        Task secondTask = mock(Task.class);

        when(firstTask.getId()).thenReturn(firstTaskId);
        when(secondTask.getId()).thenReturn(secondTaskId);

        ScheduleBlock firstBlock = mock(ScheduleBlock.class);
        ScheduleBlock secondBlock = mock(ScheduleBlock.class);

        when(firstBlock.getTask()).thenReturn(firstTask);
        when(firstBlock.getStartTime())
                .thenReturn(LocalDateTime.of(2026, 9, 24, 9, 0));
        when(firstBlock.getEndTime())
                .thenReturn(LocalDateTime.of(2026, 9, 24, 10, 0));

        when(secondBlock.getTask()).thenReturn(secondTask);
        when(secondBlock.getStartTime())
                .thenReturn(LocalDateTime.of(2026, 9, 24, 10, 0));
        when(secondBlock.getEndTime())
                .thenReturn(LocalDateTime.of(2026, 9, 24, 11, 30));

        
        Schedule schedule = mock(Schedule.class);

        when(schedule.getPeriodStart())
                .thenReturn(periodStart);

        when(schedule.getPeriodEnd())
                .thenReturn(periodEnd);

        when(schedule.getBlocks())
                .thenReturn(List.of(firstBlock, secondBlock));
        
        when(scheduleService.getCurrentSchedule(userId))
                .thenReturn(schedule);
        
        mockMvc.perform(
                get("/api/schedules/current")
                        .principal(authentication)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scheduledBlocks.length()")
                        .value(2)
                )
                .andExpect(jsonPath("$.scheduledBlocks[0].taskId")
                        .value(firstTaskId.toString())
                )
                .andExpect(jsonPath("$.scheduledBlocks[1].taskId")
                        .value(secondTaskId.toString())
                );

        verify(scheduleService)
                .getCurrentSchedule(userId);
    }

    @Test 
    void rejectsInvalidAuthenticatedUserIdForCurrentSchedule() throws Exception {
        when(authentication.getName())
                .thenReturn("not-a-uuid");
        
        mockMvc.perform(
                get("/api/schedules/current")
                        .principal(authentication)
        )
                .andExpect(status().isBadRequest());
        
        verify(scheduleService, never())
                .getCurrentSchedule(any());
    }

    @Test 
    void rejectsMissingAuthenticationForCurrentSchedule() throws Exception {
        mockMvc.perform(
                get("/api/schedules/current")
        )
                .andExpect(status().isBadRequest());
        
        verifyNoInteractions(scheduleService);
    }
}
