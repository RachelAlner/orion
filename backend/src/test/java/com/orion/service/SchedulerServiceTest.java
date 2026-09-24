package com.orion.service;

import com.orion.model.Availability;
import com.orion.model.Project;
import com.orion.model.Schedule;
import com.orion.model.Task;
import com.orion.model.TaskDependency;
import com.orion.model.TaskStatus;
import com.orion.model.User;
import com.orion.model.ScheduleBlock;
import com.orion.repository.AvailabilityRepository;
import com.orion.repository.ProjectRepository;
import com.orion.repository.ScheduleRepository;
import com.orion.repository.TaskDependencyRepository;
import com.orion.repository.TaskRepository;
import com.orion.scheduler.*;
import com.orion.scheduler.SchedulerInput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.DayOfWeek;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduleServiceTest {

    @Mock 
    private ScheduleRepository scheduleRepository;

    @Mock 
    private ProjectRepository projectRepository;

    @Mock 
    private TaskRepository taskRepository;

    @Mock 
    private TaskDependencyRepository dependencyRepository;

    @Mock 
    private AvailabilityRepository availabilityRepository;

    @Mock 
    private Scheduler scheduler;

    @Mock 
    private User user;

    @Mock 
    private Project project;

    @Mock 
    private Task task;

    @Mock 
    private Availability availability;

    @InjectMocks 
    private ScheduleService scheduleService;

    private UUID userId;
    private UUID projectId;
    private UUID taskId;

    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;

    @BeforeEach 
    void setUp() {
        userId = UUID.randomUUID();
        projectId = UUID.randomUUID();
        taskId = UUID.randomUUID();

        periodStart = LocalDateTime.of(2026, 9, 21, 0, 0);
        periodEnd = LocalDateTime.of(2026, 9, 28, 0, 0);
    }

    @Test 
    void generatesAndSavesSchedule() {
        defaultTask();
        when(scheduleRepository.save(any(Schedule.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ScheduleCandidate candidate = 
                new ScheduleCandidate(
                        taskId, 
                        LocalDateTime.of(2026, 9, 21, 9, 0),
                        LocalDateTime.of(2026, 9, 21, 10, 30)
                );
        
        ScheduleResult schedulerResult = 
                new ScheduleResult(
                        List.of(candidate),
                        List.of()
                );

        when(scheduler.generate(any(SchedulerInput.class)))
                .thenReturn(schedulerResult);

        ScheduleResult result = 
                scheduleService.generateSchedule(
                        user, 
                        periodStart, 
                        periodEnd
                );
        
        assertSame(schedulerResult, result);

        verify(scheduler).generate(any(SchedulerInput.class));
        verify(scheduleRepository).save(any(Schedule.class));
    }

    @Test 
    void passesCorrectTasksToScheduler() {
        defaultTask();
        when(scheduleRepository.save(any(Schedule.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ScheduleResult schedulerResult = 
                new ScheduleResult(
                        List.of(),
                        List.of()
                );
        
        when(scheduler.generate(any(SchedulerInput.class)))
                .thenReturn(schedulerResult);
        
        ArgumentCaptor<SchedulerInput> inputCaptor = 
                ArgumentCaptor.forClass(
                        SchedulerInput.class
                );
        
        scheduleService.generateSchedule(
                user, 
                periodStart, 
                periodEnd
        );

        verify(scheduler).generate(inputCaptor.capture());

        SchedulerInput input = 
                inputCaptor.getValue();

        assertEquals(
                1, 
                input.tasks().size()
        );

        SchedulingTask schedulingTask = 
                input.tasks().get(0);
        
        assertEquals(
                taskId, 
                schedulingTask.taskId()
        );

        assertEquals(
                90, 
                schedulingTask.estimatedMinutes()
        );

        assertEquals(
                2, 
                schedulingTask.priority()
        );

        assertEquals(
                TaskStatus.TODO, 
                schedulingTask.status()
        );

        assertEquals(
                LocalDateTime.of(2026, 9, 25, 17, 0),
                schedulingTask.deadline()
        );
    }

    @Test 
    void passesSchedulingPeriodToScheduler() {
        defaultTask();
        when(scheduleRepository.save(any(Schedule.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        when(scheduler.generate(any(SchedulerInput.class)))
                .thenReturn(
                        new ScheduleResult(
                                List.of(), 
                                List.of()
                        )
                );

        ArgumentCaptor<SchedulerInput> inputCaptor = 
                ArgumentCaptor.forClass(
                        SchedulerInput.class
                );
        
        scheduleService.generateSchedule(
                user, 
                periodStart, 
                periodEnd
        );


        verify(scheduler)
                .generate(inputCaptor.capture());
        
        SchedulerInput input = 
                inputCaptor.getValue();
        
        assertEquals(
                periodStart, 
                input.periodStart()
        );

        assertEquals(
                periodEnd, 
                input.periodEnd()
        );
    }

    @Test 
    void passesEmptyExisitingScheduleWhenNoPerviousScheduleExists() {
        defaultTask();
        when(scheduleRepository.save(any(Schedule.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        when(scheduleRepository
                .findAllByUserOrderByGeneratedAtDesc(user))
                .thenReturn(List.of());
        
        when(scheduler.generate(any(SchedulerInput.class)))
                .thenReturn(
                        new ScheduleResult(
                                List.of(), 
                                List.of()
                        )
                );

        ArgumentCaptor<SchedulerInput> inputCaptor = 
                ArgumentCaptor.forClass(
                        SchedulerInput.class
                );

        scheduleService.generateSchedule(
                user, 
                periodStart, 
                periodEnd
        );

        verify(scheduler).generate(inputCaptor.capture());

        SchedulerInput input = inputCaptor.getValue();

        assertNotNull(input.existingScheduleBlocks());

        assertTrue(input.existingScheduleBlocks().isEmpty());


    }

    @Test 
    void passesExistingScheduleBlocksToScheduler() {
        defaultTask();
        when(scheduleRepository.save(any(Schedule.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Schedule previousSchedule = mock(Schedule.class);

        ScheduleBlock previousBlock = mock(ScheduleBlock.class);

        UUID previousTaskId = UUID.randomUUID();

        Task previousTask = mock(Task.class);

        when(previousTask.getId())
                .thenReturn(previousTaskId);

        LocalDateTime blockStart = LocalDateTime.of(2026, 9, 21, 9, 0);
        LocalDateTime blockEnd = LocalDateTime.of(2026, 9, 21, 10, 0);

        when(previousBlock.getTask())
                .thenReturn(previousTask);

        when(previousBlock.getStartTime())
                .thenReturn(blockStart);

        when(previousBlock.getEndTime())
                .thenReturn(blockEnd);

        when(previousSchedule.getBlocks())
                .thenReturn(List.of(previousBlock));
        
        when(scheduleRepository
                .findAllByUserOrderByGeneratedAtDesc(user))
                .thenReturn(List.of(previousSchedule));
        
        when(scheduler.generate(any(SchedulerInput.class)))
                .thenReturn(
                        new ScheduleResult(
                                List.of(), 
                                List.of()
                        )
                );

        ArgumentCaptor<SchedulerInput> inputCaptor = 
                ArgumentCaptor.forClass(
                        SchedulerInput.class
                );

        scheduleService.generateSchedule(
                user, 
                periodStart, 
                periodEnd
        );

        verify(scheduler).generate(inputCaptor.capture());

        SchedulerInput input = inputCaptor.getValue();

        assertEquals(1, input.existingScheduleBlocks().size());

        ExistingScheduleBlock existingBlock = 
                input.existingScheduleBlocks().get(0);

        assertEquals(
                previousTaskId, 
                existingBlock.taskId()
        );

        assertEquals(
                blockStart, 
                existingBlock.startTime()
        );

        assertEquals(
                blockEnd, 
                existingBlock.endTime()
        );
    }

    @Test 
    void convertScheduledCandidatesIntoScheduleBlocks() {
        defaultTask();
        when(scheduleRepository.save(any(Schedule.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LocalDateTime blockStart = 
                LocalDateTime.of(2026, 9, 21, 9, 0);

        LocalDateTime blockEnd = 
                LocalDateTime.of(2026, 9, 21, 10, 30);

        ScheduleCandidate candidate = 
                new ScheduleCandidate(
                        taskId, 
                        blockStart, 
                        blockEnd
                );

        ScheduleResult schedulerResult = 
                new ScheduleResult(
                        List.of(candidate),
                        List.of()
                );
        
        when(scheduler.generate(any(SchedulerInput.class)))
                .thenReturn(schedulerResult);
        
        scheduleService.generateSchedule(
                user, 
                periodStart, 
                periodEnd
        );

        ArgumentCaptor<Schedule> scheduleCaptor = 
                ArgumentCaptor.forClass(
                        Schedule.class
                );

        verify(scheduleRepository)
                .save(scheduleCaptor.capture());

        Schedule savedSchedule = scheduleCaptor.getValue();

        assertEquals(1, savedSchedule.getBlocks().size());

        ScheduleBlock savedBlock = 
                savedSchedule.getBlocks().get(0);
        
        assertEquals(task, savedBlock.getTask());

        assertEquals(blockStart, savedBlock.getStartTime());
        assertEquals(blockEnd, savedBlock.getEndTime());
    }

    @Test 
    void savesCorrectSchedulePeriod() {
        defaultTask();
        when(scheduleRepository.save(any(Schedule.class))).thenAnswer(invocation -> invocation.getArgument(0));

        when(scheduler.generate(any(SchedulerInput.class)))
                .thenReturn(
                        new ScheduleResult(
                                List.of(), 
                                List.of()
                        )
                );
        
        scheduleService.generateSchedule(
                user, 
                periodStart, 
                periodEnd
        );

        ArgumentCaptor<Schedule> scheduleCaptor = 
                ArgumentCaptor.forClass(
                        Schedule.class
                );
        
        verify(scheduleRepository)
                .save(scheduleCaptor.capture());
        
        Schedule savedSchedule = 
                scheduleCaptor.getValue();
        
        assertEquals(user, savedSchedule.getUser());
        assertEquals(periodStart, savedSchedule.getPeriodStart());
        assertEquals(periodEnd, savedSchedule.getPeriodEnd());
        assertNotNull(savedSchedule.getGeneratedAt());
    }

    @Test 
    void preservesUnscheduledTasksInResult() {
        defaultTask();
        when(scheduleRepository.save(any(Schedule.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UUID unscheduledTaskId = UUID.randomUUID();

        UnscheduledTask unscheduledTask = 
                new UnscheduledTask(
                        unscheduledTaskId, 
                        60, 
                        UnscheduledReason.INSUFFICIENT_AVAILABILITY
                );
        
        ScheduleResult schedulerResult = 
                new ScheduleResult(
                        List.of(), 
                        List.of(unscheduledTask)
                );
        
        when(scheduler.generate(any(SchedulerInput.class)))
                .thenReturn(schedulerResult);
        
        ScheduleResult result = 
                scheduleService.generateSchedule(
                        user, 
                        periodStart, 
                        periodEnd
                );
        
        assertEquals(
                1, 
                result.unscheduledTasks().size()
        );

        assertEquals(
                unscheduledTaskId, 
                result.unscheduledTasks()
                        .get(0)
                        .taskId()
        );

        assertEquals(
                60, 
                result.unscheduledTasks()
                        .get(0)
                        .remainingMinutes()
        );

        assertEquals(
                UnscheduledReason.INSUFFICIENT_AVAILABILITY, 
                result.unscheduledTasks()
                        .get(0)
                        .reason()
        );
    }

    @Test 
    void convertAvailabilityIntoConcreteWindows() {
        defaultTask();
        when(scheduleRepository.save(any(Schedule.class))).thenAnswer(invocation -> invocation.getArgument(0));

        when(availabilityRepository
                .findByUserIdOrderByDayOfWeekAscStartTimeAsc(userId))
                .thenReturn(List.of(availability));
        
        when(availability.getDayOfWeek())
                .thenReturn(DayOfWeek.MONDAY);
        
        when(availability.getStartTime())
                .thenReturn(LocalTime.of(9, 0));
        
        when(availability.getEndTime())
                .thenReturn(LocalTime.of(12, 0));
        
        when(scheduler.generate(any(SchedulerInput.class)))
                .thenReturn(
                        new ScheduleResult(
                                List.of(), 
                                List.of()
                        )
                );
        
        ArgumentCaptor<SchedulerInput> inputCaptor = 
                ArgumentCaptor.forClass(
                        SchedulerInput.class
                );
        
        scheduleService.generateSchedule(
                user, 
                periodStart, 
                periodEnd
        );

        verify(scheduler)
                .generate(inputCaptor.capture());
        
        SchedulerInput input = inputCaptor.getValue();

        assertEquals(1, input.availabilityWindows().size());

        AvailabilityWindow window = 
                input.availabilityWindows().get(0);
        
        assertEquals(
                LocalDateTime.of(2026, 9, 21, 9, 0),
                window.startTime()
        );

        assertEquals(
                LocalDateTime.of(2026, 9, 21, 12, 0),
                window.endTime()
        );


    }

    @Test 
    void rejectsNullPeriodStart() {
        assertThrows(
                IllegalArgumentException.class, 
                () -> scheduleService.generateSchedule(
                        user, 
                        null, 
                        periodEnd
                )
        );

        verifyNoInteractions(
                projectRepository, 
                taskRepository, 
                dependencyRepository, 
                availabilityRepository, 
                scheduleRepository, 
                scheduler
        );
    }

    @Test 
    void rejectsNullPeriodEnd() {
        assertThrows(
                IllegalArgumentException.class, 
                () -> scheduleService.generateSchedule(
                        user, 
                        periodStart, 
                        null
                )
        );

        verifyNoInteractions(
                projectRepository, 
                taskRepository, 
                dependencyRepository, 
                availabilityRepository, 
                scheduleRepository, 
                scheduler
        );
    }

    @Test 
    void rejectsPeriodWhenStartIsAfterEnd() {
        LocalDateTime invalidStart = 
                LocalDateTime.of(2026, 9, 28, 0, 0);
        
        LocalDateTime invalidEnd = 
                LocalDateTime.of(2026, 9, 21, 0, 0);
        
        assertThrows(
                IllegalArgumentException.class, 
                () -> scheduleService.generateSchedule(
                    user, 
                    invalidStart, 
                    invalidEnd
                )
        );

        verifyNoInteractions(
                projectRepository, 
                taskRepository, 
                dependencyRepository, 
                availabilityRepository, 
                scheduleRepository, 
                scheduler
        );
    }

    @Test 
    void doesNotPersistScheduleBlockForUnscheduledTask() {
        defaultTask();
        when(scheduleRepository.save(any(Schedule.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UnscheduledTask unscheduledTask = 
                new UnscheduledTask(
                        taskId, 
                        90, 
                        UnscheduledReason.INSUFFICIENT_AVAILABILITY
                );
        
        ScheduleResult schedulerResult = 
                new ScheduleResult(
                        List.of(), 
                        List.of(unscheduledTask)
                );
        
        when(scheduler.generate(any(SchedulerInput.class)))
                .thenReturn(schedulerResult);
        
        scheduleService.generateSchedule(
                user, 
                periodStart, 
                periodEnd
        );

        ArgumentCaptor<Schedule> scheduleCaptor = 
                ArgumentCaptor.forClass(
                        Schedule.class
                );
        
        verify(scheduleRepository)
                .save(scheduleCaptor.capture());
        
        Schedule savedSchedule = 
                scheduleCaptor.getValue();
        
        assertTrue(savedSchedule.getBlocks().isEmpty());
    }

    @Test 
    void throwsWhenSchedulerReferencesTaskThatDoesNotBelongToUser() {
        defaultTask();

        UUID unknownTaskId = UUID.randomUUID();

        ScheduleCandidate candidate = 
                new ScheduleCandidate(
                        unknownTaskId, 
                        LocalDateTime.of(2026, 9, 21, 9, 0),
                        LocalDateTime.of(2026, 9, 21, 10, 0)
                );
        
        when(scheduler.generate(any(SchedulerInput.class)))
                .thenReturn(new ScheduleResult(
                        List.of(candidate),
                        List.of()
                ));
        
        assertThrows(
                IllegalStateException.class, 
                () -> scheduleService.generateSchedule(
                        user, 
                        periodStart, 
                        periodEnd
                )
        );

        verify(scheduleRepository, never())
                .save(any(Schedule.class));
    }

    private void defaultTask() {
        when(user.getId()).thenReturn(userId);
        when(project.getId()).thenReturn(projectId);
        when(task.getId()).thenReturn(taskId);

        when(task.getEstimatedMinutes())
                .thenReturn(90);
        
        when(task.getDeadline())
                .thenReturn(LocalDateTime.of(2026, 9, 25, 17, 0));
        
        when(task.getPriority())
                .thenReturn(2);
        
        when(task.getStatus())
                .thenReturn(TaskStatus.TODO);
        
        when(task.getCreatedAt())
                .thenReturn(OffsetDateTime.of(2026, 9, 20, 10, 0, 0, 0, java.time.ZoneOffset.UTC));

        when(projectRepository.findByUserId(userId)).thenReturn(List.of(project));
        when(taskRepository.findByProjectId(projectId)).thenReturn(List.of(task));
        when(dependencyRepository.findByIdTaskId(taskId)).thenReturn(List.of());
        when(availabilityRepository.findByUserIdOrderByDayOfWeekAscStartTimeAsc(userId)).thenReturn(List.of());
        when(scheduleRepository.findAllByUserOrderByGeneratedAtDesc(user)).thenReturn(List.of());
    }
}