package com.orion.scheduler;

import com.orion.model.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;


public class Scheduler {

    public ScheduleResult generate(SchedulerInput input) {
        validateInput(input);

        List<SchedulingTask> eligibleTasks = 
                getEligibleTasks(input);

        List<SchedulingTask> orderedTasks = 
                orderTasks(eligibleTasks);
        
        Map<UUID, List<UUID>> dependencies = 
                buildDependencies(input);
        
        Map<UUID, Integer> remainingMinutes = 
                initialiseRemainingMinutes(
                        eligibleTasks, 
                        input.existingScheduleBlocks()
                );

        Set<UUID> satisfiedTasks = 
                initialiseSatisfiedTasks(
                        input.tasks(), 
                        remainingMinutes
                );
        
        List<ScheduleCandidate> scheduledBlocks = 
                initialiseExistingBlocks(input);
        
        List<AvailabilityWindow> availabilityWindows = 
                orderAvailability(input);
        
        for (AvailabilityWindow availabilityWindow : availabilityWindows) {
            List<AvailabilityWindow> freeWindows = 
                    removeOccupiedTime(
                            availabilityWindow, 
                            scheduledBlocks
                    );
            
            for (AvailabilityWindow window : freeWindows) {
                allocateAvailabilityWindow(
                        window, 
                        orderedTasks, 
                        remainingMinutes, 
                        dependencies, 
                        satisfiedTasks, 
                        scheduledBlocks
                );
            }
        }

        List<UnscheduledTask> unscheduledTasks = 
                buildUnscheduledTasks(
                        orderedTasks, 
                        remainingMinutes, 
                        dependencies, 
                        satisfiedTasks, 
                        input.availabilityWindows(), 
                        scheduledBlocks
                );
        
        validateResult(
                scheduledBlocks, 
                input
        );

        return new ScheduleResult(
                scheduledBlocks, 
                unscheduledTasks
        );

    }

    private void validateInput(SchedulerInput input) {
        if (input == null) {
            throw new IllegalArgumentException(
                    "Scheduler input must not be null"
            );
        }

        if (input.periodStart() == null 
                || input.periodEnd() == null) {
            throw new IllegalArgumentException(
                    "Scheduling period must be provided"
            );
        }

        if (!input.periodStart().isBefore(input.periodEnd())) {
            throw new IllegalArgumentException(
                    "Scheduling period start must be before period end"
            );
        }

        if (input.tasks() == null
                || input.dependencies() == null
                || input.availabilityWindows() == null
                || input.existingScheduleBlocks() == null) {
            throw new IllegalArgumentException(
                    "Scheduler collections must not be null"
            );
        }
    }

    private List<SchedulingTask> getEligibleTasks(
            SchedulerInput input
    ) {
        return input.tasks().stream()
                .filter(this::isEligibleForScheduling)
                .toList();
    }

    private boolean isEligibleForScheduling(SchedulingTask task) {
        return task.status() != TaskStatus.COMPLETED
                && task.status() != TaskStatus.CANCELLED
                && task.remainingMinutes() > 0;
    }

    private Map<UUID, List<UUID>> buildDependencies(
            SchedulerInput input 
    ) {
        return input.dependencies().stream()
                .collect(Collectors.groupingBy(
                        TaskDependency::taskId, 
                        Collectors.mapping(
                                TaskDependency::dependsOnTaskId, 
                                Collectors.toList()
                        )
                ));
    }

    private List<SchedulingTask> orderTasks(
            List<SchedulingTask> tasks
    ) {
        return tasks.stream()
                .sorted(
                        Comparator
                                .comparing(
                                        SchedulingTask::deadline,
                                        Comparator.nullsLast(
                                                Comparator.naturalOrder()
                                        )
                                )
                                .thenComparing(
                                        SchedulingTask::priority, 
                                        Comparator.reverseOrder()
                                )
                                .thenComparing(
                                        SchedulingTask::createdAt
                                )
                                .thenComparing(
                                        SchedulingTask::taskId
                                )
                )
                .toList();
    }

    private List<AvailabilityWindow> orderAvailability(
            SchedulerInput input
    ) {
        return input.availabilityWindows()
                .stream()
                .sorted(
                        Comparator
                                .comparing(
                                        AvailabilityWindow::startTime
                                )
                                .thenComparing(
                                        AvailabilityWindow::endTime
                                )
                )
                .toList();
    }

    private Map<UUID, Integer> initialiseRemainingMinutes(
            List<SchedulingTask> tasks, 
            List<ExistingScheduleBlock> existingBlocks
    ) {
        Map<UUID, Integer> remainingMinutes = 
                tasks.stream()
                        .collect(Collectors.toMap(
                                SchedulingTask::taskId,
                                SchedulingTask::remainingMinutes
                        ));
        for (ExistingScheduleBlock block : existingBlocks) {
            UUID taskId = block.taskId();

            if (!remainingMinutes.containsKey(taskId)) {
                continue;
            }

            int allocatedMinutes = 
                    (int) Duration.between(
                            block.startTime(), 
                            block.endTime()
                    ).toMinutes();
            
            int remaining = 
                    Math.max(
                            0, 
                            remainingMinutes.get(taskId)
                                    - allocatedMinutes
                    );
            remainingMinutes.put(taskId, remaining);
        }

        return remainingMinutes;
    }

    private List<ScheduleCandidate> initialiseExistingBlocks(
            SchedulerInput input 
    ) {
        return input.existingScheduleBlocks()
                .stream()
                .map(block -> 
                        new ScheduleCandidate(
                                block.taskId(), 
                                block.startTime(), 
                                block.endTime()
                        )
                )
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private Set<UUID> initialiseSatisfiedTasks(
            List<SchedulingTask> tasks,
            Map<UUID, Integer> remainingMinutes
    ) {
        return tasks.stream()
                .filter(task -> isTaskSatisfied(task, remainingMinutes))
                .map(SchedulingTask::taskId)
                .collect(Collectors.toCollection(HashSet::new));
    }

    private boolean isTaskSatisfied(
            SchedulingTask task,
            Map<UUID, Integer> remainingMinutes
    ) {
        if (task.status() == TaskStatus.COMPLETED) {
            return true;
        }

        Integer remaining = remainingMinutes.get(task.taskId());
        return remaining != null && remaining == 0;
    }

    private boolean dependenciesSatisfied(
            UUID taskId, 
            Map<UUID, List<UUID>> dependencies,
            Set<UUID> satisfiedTasks
    ) {
        return dependencies
                .getOrDefault(taskId, List.of())
                .stream()
                .allMatch(satisfiedTasks::contains);
    }

    private SchedulingTask findNextTask(
            List<SchedulingTask> orderedTasks,
            Map<UUID, Integer> remainingMinutes,
            Map<UUID, List<UUID>> dependencies,
            Set<UUID> satisfiedTasks,
            LocalDateTime currentTime
    ) {
        return orderedTasks.stream()
                .filter(task -> hasRemainingWork(task, remainingMinutes))
                .filter(task -> dependenciesSatisfied(task.taskId(), dependencies, satisfiedTasks))
                .filter(task -> canScheduleBeforeDeadline(task, currentTime))
                .findFirst()
                .orElse(null);
    }

    private boolean hasRemainingWork(
            SchedulingTask task,
            Map<UUID, Integer> remainingMinutes
    ) {
        Integer remaining = remainingMinutes.get(task.taskId());
        return remaining != null && remaining > 0;
    }

    private boolean canScheduleBeforeDeadline(
            SchedulingTask task,
            LocalDateTime currentTime
    ) {
        return task.deadline() == null || currentTime.isBefore(task.deadline());
    }

    private void allocateAvailabilityWindow(
            AvailabilityWindow window, 
            List<SchedulingTask> orderedTasks, 
            Map<UUID, Integer> remainingMinutes, 
            Map<UUID, List<UUID>> dependencies,
            Set<UUID> satisfiedTasks, 
            List<ScheduleCandidate> scheduledBlocks
    ) {
        LocalDateTime currentTime = window.startTime();

        while (currentTime.isBefore(window.endTime())) {

            SchedulingTask task = findNextTask(
                    orderedTasks, 
                    remainingMinutes, 
                    dependencies, 
                    satisfiedTasks, 
                    currentTime
            );

            if (task == null) {
                break;
            }

            LocalDateTime allocationEnd = 
                    calculateAllocationEnd(
                            task, 
                            remainingMinutes.get(task.taskId()),
                            currentTime, 
                            window.endTime()
                    );
            
            if(!currentTime.isBefore(allocationEnd)) {
                break;
            }

            int allocatedMinutes = 
                    (int) Duration.between(
                            currentTime, 
                            allocationEnd
                    ).toMinutes();
            
            scheduledBlocks.add(
                    new ScheduleCandidate(
                            task.taskId(), 
                            currentTime, 
                            allocationEnd
                    )
            );

            int remaining = 
                    remainingMinutes.get(task.taskId()) 
                            - allocatedMinutes;
            
            remainingMinutes.put(
                    task.taskId(), 
                    remaining
            );

            if (remaining == 0) {
                satisfiedTasks.add(task.taskId());
            }

            currentTime = allocationEnd;
        }
    }

    private LocalDateTime calculateAllocationEnd(
            SchedulingTask task, 
            int remainingMinutes,
            LocalDateTime currentTime, 
            LocalDateTime availabilityEnd
    ) {
        LocalDateTime latestAllowedEnd = availabilityEnd;

        if (task.deadline() != null
                && task.deadline().isBefore(latestAllowedEnd)) {
            latestAllowedEnd = task.deadline();
        }

        long availableMinutes = 
                Duration.between(
                        currentTime, 
                        latestAllowedEnd
                ).toMinutes();

        long minutesToAllocate = 
                Math.min(
                        availableMinutes, 
                        remainingMinutes
                );

        return currentTime.plusMinutes(minutesToAllocate);
    }

    private List<UnscheduledTask> buildUnscheduledTasks(
            List<SchedulingTask> orderedTasks, 
            Map<UUID, Integer> remainingMinutes, 
            Map<UUID, List<UUID>> dependencies, 
            Set<UUID> satisfiedTasks, 
            List<AvailabilityWindow> availabilityWindows, 
            List<ScheduleCandidate> scheduledBlocks
    ) {
        List<UnscheduledTask> unscheduledTasks = 
                new ArrayList<>();
        
        for (SchedulingTask task : orderedTasks) {

            int remaining = remainingMinutes.get(task.taskId());

            if (remaining <= 0) {
                continue;
            }

            UnscheduledReason reason;

            if (!dependenciesSatisfied(
                    task.taskId(), 
                    dependencies, 
                    satisfiedTasks
            )) {
                reason = UnscheduledReason.DEPENDENCY_BLOCKED;
            } else if (cannotMeetDeadline(
                    task, 
                    remaining, 
                    availabilityWindows, 
                    scheduledBlocks
            ) && hasAvailabilityAfterDeadline(
                    task, 
                    availabilityWindows
            )) {
                reason = UnscheduledReason.DEADLINE_UNACHIEVABLE;
            } else {
                reason = UnscheduledReason.INSUFFICIENT_AVAILABILITY;
            }

            unscheduledTasks.add(
                    new UnscheduledTask(
                            task.taskId(), 
                            remaining, 
                            reason
                    )
            );
        }

        return unscheduledTasks;
    }

    private void validateResult(
            List<ScheduleCandidate> scheduledBlocks,
            SchedulerInput input
    ) {
        for (ScheduleCandidate block : scheduledBlocks) {
            if (!block.startTime().isBefore(block.endTime())) {
                throw new IllegalStateException(
                        "Schedule block must have a positive duration"
                );
            }

            if (block.startTime().isBefore(input.periodStart())
                    || block.endTime().isAfter(input.periodEnd())) {
                throw new IllegalStateException(
                        "Schedule block must be within scheduling period"
                );
            }

            if (!isWithinAvailability(
                    block, 
                    input.availabilityWindows()
            )) {
                throw new IllegalStateException(
                        "Schedule block must be within availability"
                );
            }
        }

        validateNoOverlaps(scheduledBlocks);
    }

    private boolean isWithinAvailability(
            ScheduleCandidate block, 
            List<AvailabilityWindow> availabilityWindows
    ) {
        return availabilityWindows.stream()
                .anyMatch(window -> 
                        !block.startTime()
                                .isBefore(window.startTime())
                                && !block.endTime()
                                .isAfter(window.endTime())
                );
    }
    
    private void validateNoOverlaps(
            List<ScheduleCandidate> scheduledBlocks
    ) {
        List<ScheduleCandidate> sortedBlocks = scheduledBlocks.stream()
                .sorted(Comparator.comparing(ScheduleCandidate::startTime))
                .toList();

        for (int i = 0; i < sortedBlocks.size() - 1; i++) {
            ScheduleCandidate current = sortedBlocks.get(i);
            ScheduleCandidate next = sortedBlocks.get(i + 1);

            if (isDuplicateBlock(current, next)) {
                continue;
            }

            if (blocksOverlap(current, next)) {
                throw new IllegalStateException("Schedule blocks must not overlap");
            }
        }
    }

    private boolean isDuplicateBlock(
            ScheduleCandidate current,
            ScheduleCandidate next
    ) {
        return current.taskId().equals(next.taskId())
                && current.startTime().equals(next.startTime())
                && current.endTime().equals(next.endTime());
    }

    private boolean blocksOverlap(
            ScheduleCandidate current,
            ScheduleCandidate next
    ) {
        return current.startTime().isBefore(next.endTime())
                && current.endTime().isAfter(next.startTime());
    }

    private List<AvailabilityWindow> removeOccupiedTime(
            AvailabilityWindow availability,
            List<ScheduleCandidate> scheduledBlocks
    ) {
        List<AvailabilityWindow> result = new ArrayList<>();
        LocalDateTime cursor = availability.startTime();

        List<ScheduleCandidate> overlappingBlocks = getOverlappingBlocks(availability, scheduledBlocks);

        for (ScheduleCandidate block : overlappingBlocks) {
            if (cursor.isBefore(block.startTime())) {
                result.add(new AvailabilityWindow(cursor, block.startTime()));
            }

            if (cursor.isBefore(block.endTime())) {
                cursor = block.endTime();
            }
        }

        if (cursor.isBefore(availability.endTime())) {
            result.add(new AvailabilityWindow(cursor, availability.endTime()));
        }

        return result;
    }

    private List<ScheduleCandidate> getOverlappingBlocks(
            AvailabilityWindow availability,
            List<ScheduleCandidate> scheduledBlocks
    ) {
        return scheduledBlocks.stream()
                .filter(block -> blocksOverlapWithAvailability(block, availability))
                .sorted(Comparator.comparing(ScheduleCandidate::startTime))
                .toList();
    }

    private boolean blocksOverlapWithAvailability(
            ScheduleCandidate block,
            AvailabilityWindow availability
    ) {
        return block.startTime().isBefore(availability.endTime())
                && block.endTime().isAfter(availability.startTime());
    }

    private boolean cannotMeetDeadline(
        SchedulingTask task,
        int remainingMinutes,
        List<AvailabilityWindow> availabilityWindows,
        List<ScheduleCandidate> scheduledBlocks
    ) {
        if (task.deadline() == null) {
            return false;
        }

        long availableMinutes = calculateAvailableMinutesBefore(
                task.deadline(),
                availabilityWindows,
                scheduledBlocks
        );

        return availableMinutes < remainingMinutes;
    }

    private long calculateAvailableMinutesBefore(
            LocalDateTime deadline,
            List<AvailabilityWindow> availabilityWindows,
            List<ScheduleCandidate> scheduledBlocks
    ) {
        long availableMinutes = 0;

        for (AvailabilityWindow availability : availabilityWindows) {
            LocalDateTime windowEnd = availability.endTime().isBefore(deadline)
                    ? availability.endTime()
                    : deadline;

            if (!availability.startTime().isBefore(windowEnd)) {
                continue;
            }

            AvailabilityWindow truncatedWindow = 
                    new AvailabilityWindow(
                            availability.startTime(),
                            windowEnd
                    );

            List<AvailabilityWindow> freeWindows = 
                    removeOccupiedTime(truncatedWindow, scheduledBlocks);

            for (AvailabilityWindow freeWindow : freeWindows) {
                availableMinutes += Duration.between(
                        freeWindow.startTime(),
                        freeWindow.endTime()
                ).toMinutes();
            }
        }

        return availableMinutes;
    }

    private boolean hasAvailabilityAfterDeadline(
            SchedulingTask task, 
            List<AvailabilityWindow> availabilityWindows
    ) {
        if (task.deadline() == null) {
            return false;
        }

        return availabilityWindows.stream()
                .anyMatch(window -> 
                        window.endTime().isAfter(task.deadline())
                );
    }

}