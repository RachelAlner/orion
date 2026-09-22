package com.orion.scheduler;

import com.orion.model.TaskStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SchedulerTest {

    private final Scheduler scheduler = new Scheduler();

    @Test 
    void schedulesTaskWithinAvailableWindow() {
        UUID taskId = UUID.randomUUID();

        LocalDateTime periodStart = 
                LocalDateTime.of(2026, 9, 21, 0, 0);
        
        LocalDateTime periodEnd = 
                LocalDateTime.of(2026, 9, 22, 0, 0);
        
        SchedulingTask task = task(
                taskId, 
                60, 
                LocalDateTime.of(2026, 9, 21, 17, 0),
                3, 
                LocalDateTime.of(2026, 9, 20, 10, 0)
        );

        AvailabilityWindow availability = new AvailabilityWindow(
                LocalDateTime.of(2026, 9, 21, 9, 0),
                LocalDateTime.of(2026, 9, 21, 17, 0)
        );

        SchedulerInput input = new SchedulerInput(
                List.of(task),
                List.of(),
                List.of(availability),
                List.of(),
                periodStart,
                periodEnd
        );

        ScheduleResult result = scheduler.generate(input);

        assertEquals(1, result.scheduledBlocks().size());
        assertTrue(result.unscheduledTasks().isEmpty());

        ScheduleCandidate block = 
                result.scheduledBlocks().getFirst();
        
        assertEquals(taskId, block.taskId());
        assertEquals(
                LocalDateTime.of(2026, 9, 21, 9, 0), 
                block.startTime()
        );
        assertEquals(
                LocalDateTime.of(2026, 9, 21, 10, 0),
                block.endTime()
        );
    }

    @Test 
    void doesNotScheduleTaskOutsideAvailability() {
        UUID taskId = UUID.randomUUID();

        SchedulingTask task = task(
                taskId, 
                60, 
                LocalDateTime.of(2026, 9, 21, 17, 0),
                3, 
                LocalDateTime.of(2026, 9, 20, 10, 0)
        );

        AvailabilityWindow availability = new AvailabilityWindow(
                LocalDateTime.of(2026, 9, 21, 9, 0),
                LocalDateTime.of(2026, 9, 21, 10, 0)
        );

        SchedulerInput input = input(
                List.of(task),
                List.of(availability)
        );

        ScheduleResult result = scheduler.generate(input);

        assertEquals(1, result.scheduledBlocks().size());

        ScheduleCandidate block = 
                result.scheduledBlocks().getFirst();
        
        assertTrue(
                !block.startTime().isBefore(availability.startTime())
                && !block.endTime().isAfter(availability.endTime())
        );
    }

    @Test 
    void reportsRemainingWorkWhenThereIsInsufficientAvailabilit() {
        UUID taskId = UUID.randomUUID();

        SchedulingTask task = task(
                taskId, 
                120, 
                LocalDateTime.of(2026, 9, 21, 17, 0),
                3, 
                LocalDateTime.of(2026, 9, 20, 10, 0)
        );

        AvailabilityWindow availability = new AvailabilityWindow(
                LocalDateTime.of(2026, 9, 21, 9, 0),
                LocalDateTime.of(2026, 9, 21, 10, 0)
        );

        SchedulerInput input = input(
                List.of(task),
                List.of(availability)
        );

        ScheduleResult result = scheduler.generate(input);

        assertEquals(1, result.scheduledBlocks().size());
        assertEquals(1, result.unscheduledTasks().size());

        UnscheduledTask unscheduled = 
                result.unscheduledTasks().getFirst();
        
        assertEquals(taskId, unscheduled.taskId());
        assertEquals(60, unscheduled.remainingMinutes());
        assertEquals(
                UnscheduledReason.INSUFFICIENT_AVAILABILITY, 
                unscheduled.reason()
        );

    }

    @Test 
    void splitsTaskAcrossMultipleAvailabilityWindows() {
        UUID taskId = UUID.randomUUID();

        SchedulingTask task = task(
                taskId, 
                120, 
                LocalDateTime.of(2026, 9, 22, 17, 0),
                3, 
                LocalDateTime.of(2026, 9, 20, 10, 0)
        );

        AvailabilityWindow morning = new AvailabilityWindow(
                LocalDateTime.of(2026, 9, 21, 9, 0),
                LocalDateTime.of(2026, 9, 21, 10, 0)
        );

        AvailabilityWindow afternoon = new AvailabilityWindow(
                LocalDateTime.of(2026, 9, 21, 14, 0),
                LocalDateTime.of(2026, 9, 21, 15, 0)
        );

        SchedulerInput input = input(
                List.of(task),
                List.of(morning, afternoon)
        );

        ScheduleResult result = scheduler.generate(input);

        assertEquals(2, result.scheduledBlocks().size());
        assertTrue(result.unscheduledTasks().isEmpty());

        ScheduleCandidate first = 
                result.scheduledBlocks().get(0);

        ScheduleCandidate second = 
                result.scheduledBlocks().get(1);

        assertEquals(taskId, first.taskId());
        assertEquals(taskId, second.taskId());

        assertEquals(
                LocalDateTime.of(2026, 9, 21, 9, 0), 
                first.startTime()
        );
        assertEquals(
                LocalDateTime.of(2026, 9, 21, 10, 0), 
                first.endTime()
        );

        assertEquals(
                LocalDateTime.of(2026, 9, 21, 14, 0), 
                second.startTime()
        );
        assertEquals(
                LocalDateTime.of(2026, 9, 21, 15, 0), 
                second.endTime()
        );

    }

    @Test 
    void doesNotScheduleCompletedTask() {
        UUID taskId = UUID.randomUUID();

        SchedulingTask task = new SchedulingTask(
                taskId, 
                60, 
                60,
                LocalDateTime.of(2026, 9, 21, 17, 0),
                3, 
                TaskStatus.COMPLETED,
                LocalDateTime.of(2026, 9, 20, 10, 0)
        );

        AvailabilityWindow availability = new AvailabilityWindow(
                LocalDateTime.of(2026, 9, 21, 9, 0),
                LocalDateTime.of(2026, 9, 21, 17, 0)
        );

        SchedulerInput input = input(
                List.of(task),
                List.of(availability)
        );

        ScheduleResult result = scheduler.generate(input);

        assertTrue(result.scheduledBlocks().isEmpty());
        assertTrue(result.unscheduledTasks().isEmpty());

    }

    @Test 
    void doesNotScheduleCancelledTask() {
        UUID taskId = UUID.randomUUID();

        SchedulingTask task = new SchedulingTask(
                taskId, 
                60, 
                60,
                LocalDateTime.of(2026, 9, 21, 17, 0),
                3, 
                TaskStatus.CANCELLED,
                LocalDateTime.of(2026, 9, 20, 10, 0)
        );

        AvailabilityWindow availability = new AvailabilityWindow(
                LocalDateTime.of(2026, 9, 21, 9, 0),
                LocalDateTime.of(2026, 9, 21, 17, 0)
        );

        SchedulerInput input = input(
                List.of(task),
                List.of(availability)
        );

        ScheduleResult result = scheduler.generate(input);

        assertTrue(result.scheduledBlocks().isEmpty());
        assertTrue(result.unscheduledTasks().isEmpty());

    }

    @Test 
    void schedulesHigherPriorityTaskFirstWhenTasksCompeteForSameWindow() {
        UUID lowPriorityTaskId = UUID.randomUUID();
        UUID highPriorityTaskId = UUID.randomUUID();

        SchedulingTask lowPriorityTask = task(
                lowPriorityTaskId, 
                60, 
                LocalDateTime.of(2026, 9, 21, 17, 0),
                1, 
                LocalDateTime.of(2026, 9, 20, 10, 0)
        );

        SchedulingTask highPriorityTask = task(
                highPriorityTaskId, 
                60, 
                LocalDateTime.of(2026, 9, 21, 17, 0),
                5, 
                LocalDateTime.of(2026, 9, 20, 11, 0)
        );

        AvailabilityWindow availability = new AvailabilityWindow(
                LocalDateTime.of(2026, 9, 21, 9, 0),
                LocalDateTime.of(2026, 9, 21, 10, 0)
        );

        SchedulerInput input = input(
                List.of(lowPriorityTask, highPriorityTask),
                List.of(availability)
        );

        ScheduleResult result = scheduler.generate(input);

        assertEquals(1, result.scheduledBlocks().size());
        assertEquals(1, result.unscheduledTasks().size());

        ScheduleCandidate scheduled = 
                result.scheduledBlocks().getFirst();
        
        assertEquals(highPriorityTaskId, scheduled.taskId());

        UnscheduledTask unscheduled = 
                result.unscheduledTasks().getFirst();
        
        assertEquals(lowPriorityTaskId, unscheduled.taskId());
    }

    @Test 
    void schedulesEarlierDeadlineFirstWhenPriorityIsEqual() {
        UUID laterDeadlineTaskId = UUID.randomUUID();
        UUID earlierDeadlineTaskId = UUID.randomUUID();

        SchedulingTask laterDeadlineTask = task(
                laterDeadlineTaskId, 
                60, 
                LocalDateTime.of(2026, 9, 22, 17, 0),
                3, 
                LocalDateTime.of(2026, 9, 20, 10, 0)
        );

        SchedulingTask earlierDeadlineTask = task(
                earlierDeadlineTaskId, 
                60, 
                LocalDateTime.of(2026, 9, 21, 17, 0),
                3, 
                LocalDateTime.of(2026, 9, 20, 11, 0)
        );

        AvailabilityWindow availability = new AvailabilityWindow(
                LocalDateTime.of(2026, 9, 21, 9, 0),
                LocalDateTime.of(2026, 9, 21, 10, 0)
        );

        SchedulerInput input = input(
                List.of(laterDeadlineTask, earlierDeadlineTask),
                List.of(availability)
        );

        ScheduleResult result = scheduler.generate(input);

        assertEquals(1, result.scheduledBlocks().size());

        ScheduleCandidate scheduled = 
                result.scheduledBlocks().getFirst();
        
        assertEquals(earlierDeadlineTaskId, scheduled.taskId());
    }

    @Test 
    void schedulesDependencyBeforeDependentTask() {
        UUID prerequisiteId = UUID.randomUUID();
        UUID dependentId = UUID.randomUUID();

        SchedulingTask prerequisite = task(
                prerequisiteId, 
                60, 
                LocalDateTime.of(2026, 9, 21, 17, 0),
                3, 
                LocalDateTime.of(2026, 9, 20, 10, 0)
        );

        SchedulingTask dependent = task(
                dependentId, 
                60, 
                LocalDateTime.of(2026, 9, 21, 17, 0),
                3, 
                LocalDateTime.of(2026, 9, 20, 11, 0)
        );

        TaskDependency dependency = new TaskDependency(
                dependentId, 
                prerequisiteId
        );

        AvailabilityWindow availability = new AvailabilityWindow(
                LocalDateTime.of(2026, 9, 21, 9, 0),
                LocalDateTime.of(2026, 9, 21, 11, 0)
        );

        SchedulerInput input = new SchedulerInput(
                List.of(dependent, prerequisite),
                List.of(dependency),
                List.of(availability),
                List.of(),
                LocalDateTime.of(2026, 9, 21, 0, 0),
                LocalDateTime.of(2026, 9, 22, 0, 0)
        );

        ScheduleResult result = scheduler.generate(input);

        assertEquals(2, result.scheduledBlocks().size());

        ScheduleCandidate first = 
                result.scheduledBlocks().get(0);

        ScheduleCandidate second = 
                result.scheduledBlocks().get(1);

        assertEquals(prerequisiteId, first.taskId());
        assertEquals(dependentId, second.taskId());

        assertEquals(
                LocalDateTime.of(2026, 9, 21, 9, 0), 
                first.startTime()
        );
        assertEquals(
                LocalDateTime.of(2026, 9, 21, 10, 0), 
                first.endTime()
        );

        assertEquals(
                LocalDateTime.of(2026, 9, 21, 10, 0), 
                second.startTime()
        );
        assertEquals(
                LocalDateTime.of(2026, 9, 21, 11, 0), 
                second.endTime()
        );

    }

    @Test 
    void doesNotScheduleDependentTaskWhenDependencyIsBlocked() {
        UUID prerequisiteId = UUID.randomUUID();
        UUID dependentId = UUID.randomUUID();

        SchedulingTask prerequisite = task(
                prerequisiteId, 
                120,  
                LocalDateTime.of(2026, 9, 21, 10, 0),
                3, 
                LocalDateTime.of(2026, 9, 20, 10, 0)
        );

        SchedulingTask dependent = task(
                dependentId, 
                60, 
                LocalDateTime.of(2026, 9, 21, 17, 0),
                3, 
                LocalDateTime.of(2026, 9, 20, 11, 0)
        );

        TaskDependency dependency = new TaskDependency(
                dependentId, 
                prerequisiteId
        );

        AvailabilityWindow availability = new AvailabilityWindow(
                LocalDateTime.of(2026, 9, 21, 9, 0),
                LocalDateTime.of(2026, 9, 21, 10, 0)
        );

        SchedulerInput input = new SchedulerInput(
                List.of(prerequisite, dependent),
                List.of(dependency),
                List.of(availability),
                List.of(),
                LocalDateTime.of(2026, 9, 21, 0, 0),
                LocalDateTime.of(2026, 9, 22, 0, 0)
        );

        ScheduleResult result = scheduler.generate(input);

        assertEquals(1, result.scheduledBlocks().size());
        assertEquals(2, result.unscheduledTasks().size());

        assertEquals(
                prerequisiteId, 
                result.scheduledBlocks().getFirst().taskId()
        );

        UnscheduledTask unscheduled = 
                result.unscheduledTasks().get(1);

        assertEquals(dependentId, unscheduled.taskId());
        assertEquals(
                UnscheduledReason.DEPENDENCY_BLOCKED,
                unscheduled.reason()
        );
    }

    @Test 
    void doesNotScheduleTaskAfterItsDealine() {
        UUID taskId = UUID.randomUUID();

        SchedulingTask task = task(
                taskId, 
                60, 
                LocalDateTime.of(2026, 9, 21, 12, 0),
                3, 
                LocalDateTime.of(2026, 9, 20, 10, 0)
        );

        AvailabilityWindow availability = new AvailabilityWindow(
                LocalDateTime.of(2026, 9, 21, 11, 0),
                LocalDateTime.of(2026, 9, 21, 14, 0)
        );

        SchedulerInput input = input(
                List.of(task),
                List.of(availability)
        );

        ScheduleResult result = scheduler.generate(input);

        assertEquals(1, result.scheduledBlocks().size());

        ScheduleCandidate block = 
                result.scheduledBlocks().getFirst();
        
        assertEquals(
                LocalDateTime.of(2026, 9, 21, 11, 0),
                block.startTime()
        );

        assertEquals(
                LocalDateTime.of(2026, 9, 21, 12, 0),
                block.endTime()
        );

    }

    @Test 
    void reportsDeadlineUnachievableWhenTaskCannotFitBeforeDeadline() {
        UUID taskId = UUID.randomUUID();

        SchedulingTask task = task(
                taskId, 
                60, 
                LocalDateTime.of(2026, 9, 21, 10, 0),
                3, 
                LocalDateTime.of(2026, 9, 20, 10, 0)
        );

        AvailabilityWindow availability = new AvailabilityWindow(
                LocalDateTime.of(2026, 9, 21, 9, 0),
                LocalDateTime.of(2026, 9, 21, 10, 0)
        );

        SchedulerInput input = input(
                List.of(task),
                List.of(availability)
        );

        ScheduleResult result = scheduler.generate(input);

        assertEquals(1, result.scheduledBlocks().size());
        assertEquals(1, result.unscheduledTasks().size());

        UnscheduledTask unscheduled = 
                result.unscheduledTasks().getFirst();

        assertEquals(taskId, unscheduled.taskId());
        assertEquals(60, unscheduled.remainingMinutes());
        assertEquals(
                UnscheduledReason.DEADLINE_UNACHIEVABLE,
                unscheduled.reason()
        );
    }

    @Test 
    void preservesExistingScheduleBlock() {
        UUID existingTaskId = UUID.randomUUID();
        UUID newTaskId = UUID.randomUUID();

        SchedulingTask existingTask = task(
                existingTaskId, 
                60,  
                LocalDateTime.of(2026, 9, 21, 17, 0),
                3, 
                LocalDateTime.of(2026, 9, 20, 10, 0)
        );

        SchedulingTask newTask = task(
                newTaskId, 
                60, 
                LocalDateTime.of(2026, 9, 21, 17, 0),
                3, 
                LocalDateTime.of(2026, 9, 20, 11, 0)
        );

        ExistingScheduleBlock existingBlock = 
                new ExistingScheduleBlock(
                        existingTaskId, 
                        LocalDateTime.of(2026, 9, 21, 9, 0),
                        LocalDateTime.of(2026, 9, 21, 10, 0)
                );

        AvailabilityWindow availability = new AvailabilityWindow(
                LocalDateTime.of(2026, 9, 21, 9, 0),
                LocalDateTime.of(2026, 9, 21, 12, 0)
        );

        SchedulerInput input = new SchedulerInput(
                List.of(existingTask, newTask),
                List.of(),
                List.of(availability),
                List.of(existingBlock),
                LocalDateTime.of(2026, 9, 21, 0, 0),
                LocalDateTime.of(2026, 9, 22, 0, 0)
        );

        ScheduleResult result = scheduler.generate(input);

        assertEquals(2, result.scheduledBlocks().size());

        ScheduleCandidate preserved = 
                result.scheduledBlocks().get(0);

        assertEquals(existingTaskId, preserved.taskId());
        assertEquals(
                existingBlock.startTime(),
                preserved.startTime()
        );
        assertEquals(
                existingBlock.endTime(), 
                preserved.endTime()
        );
    }

    @Test 
    void producesDeterministicResultForSameInput() {
        UUID firstTaskId = UUID.randomUUID();
        UUID secondTaskId = UUID.randomUUID();

        SchedulingTask firstTask = task(
                firstTaskId, 
                60,  
                LocalDateTime.of(2026, 9, 21, 17, 0),
                3, 
                LocalDateTime.of(2026, 9, 20, 10, 0)
        );

        SchedulingTask secondTask = task(
                secondTaskId, 
                60, 
                LocalDateTime.of(2026, 9, 21, 17, 0),
                3, 
                LocalDateTime.of(2026, 9, 20, 11, 0)
        );

        AvailabilityWindow availability = new AvailabilityWindow(
                LocalDateTime.of(2026, 9, 21, 9, 0),
                LocalDateTime.of(2026, 9, 21, 11, 0)
        );

        SchedulerInput input = input(
                List.of(firstTask, secondTask),
                List.of(availability)
        );

        ScheduleResult firstResult = scheduler.generate(input);

        ScheduleResult secondResult = scheduler.generate(input);

        assertEquals(
                firstResult.scheduledBlocks(), 
                secondResult.scheduledBlocks()
        );

        assertEquals(
                firstResult.unscheduledTasks(), 
                secondResult.unscheduledTasks()
        );

    }

    private SchedulerInput input(
            List<SchedulingTask> tasks, 
            List<AvailabilityWindow> availabilityWindows
    ) {
        return new SchedulerInput(
                tasks, 
                List.of(), 
                availabilityWindows,
                List.of(),
                LocalDateTime.of(2026, 9, 21, 0, 0),
                LocalDateTime.of(2026, 9, 22, 0, 0)
        );
    }

    private SchedulingTask task(
            UUID taskId, 
            int remainingMinutes, 
            LocalDateTime deadline, 
            int priority, 
            LocalDateTime createdAt
    ) {
        return new SchedulingTask(
                taskId, 
                remainingMinutes, 
                remainingMinutes, 
                deadline, 
                priority, 
                TaskStatus.TODO, 
                createdAt
        );
    }
}