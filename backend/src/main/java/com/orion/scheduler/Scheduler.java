package com.orion.scheduler;

import com.orion.model.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map;
import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Comparator;


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
                .filter(task -> 
                        task.status() != TaskStatus.COMPLETED
                )
                .filter(task -> 
                        task.status() != TaskStatus.CANCELLED
                )
                .filter(task -> 
                        task.remainingMinutes() > 0
                )
                .toList();
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
                .filter(task -> {
                    if (task.status() == TaskStatus.COMPLETED) {
                        return true;
                    }

                    Integer remaining = 
                            remainingMinutes.get(task.taskId());
                    
                    return remaining != null && remaining == 0;
                })
                .map(SchedulingTask::taskId)
                .collect(Collectors.toCollection(HashSet::new));
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
                .filter(task -> {
                    Integer remaining = 
                            remainingMinutes.get(task.taskId());
                    
                    return remaining != null && remaining > 0;
                })
                .filter(task -> 
                        dependenciesSatisfied(
                                task.taskId(), 
                                dependencies, 
                                satisfiedTasks
                        )
                )
                .filter(task -> 
                        task.deadline() == null 
                                || currentTime.isBefore(task.deadline())
                )
                .findFirst()
                .orElse(null);
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
        for (int i = 0; i < scheduledBlocks.size(); i++) {

            ScheduleCandidate first = 
                    scheduledBlocks.get(i);
            
            for (int j = i + 1; j < scheduledBlocks.size(); j++) {
                ScheduleCandidate second = 
                        scheduledBlocks.get(j);

                if (first.taskId().equals(second.taskId())
                        && first.startTime().equals(second.startTime())
                        && first.endTime().equals(second.endTime())) {
                    continue;
                }

                boolean overlaps = 
                        first.startTime().isBefore(second.endTime())
                                && first.endTime()
                                .isAfter(second.startTime());
                
                if (overlaps) {
                    throw new IllegalStateException(
                            "Schedule blocks must not overlap"
                    );
                }
            }
        }
    }

    private List<AvailabilityWindow> removeOccupiedTime(
            AvailabilityWindow availability,
            List<ScheduleCandidate> scheduledBlocks
    ) {
        List<AvailabilityWindow> result = 
                new ArrayList<>();
        
        LocalDateTime cursor = 
                availability.startTime();
        
        List<ScheduleCandidate> overlappingBlocks = 
                scheduledBlocks.stream()
                        .filter(block -> 
                                block.startTime()
                                        .isBefore(availability.endTime())
                                        && block.endTime()
                                        .isAfter(availability.startTime())
                        )
                        .sorted(
                                Comparator.comparing(
                                        ScheduleCandidate::startTime
                                )
                        )
                        .toList();

        for (ScheduleCandidate block : overlappingBlocks) {
            if (cursor.isBefore(block.startTime())) {
                result.add(
                        new AvailabilityWindow(
                                cursor, 
                                block.startTime()
                        )
                );
            }

            if (cursor.isBefore(block.endTime())) {
                cursor = block.endTime();
            }
        }

        if (cursor.isBefore(availability.endTime())) {
            result.add(
                new AvailabilityWindow(
                        cursor, 
                        availability.endTime()
                )
            );
        }

        return result;
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

        long availableMinutes = 0;

        for (AvailabilityWindow availability : availabilityWindows) {

            LocalDateTime availableEnd =
                    availability.endTime().isBefore(task.deadline())
                            ? availability.endTime()
                            : task.deadline();

            if (!availability.startTime().isBefore(availableEnd)) {
                continue;
            }

            List<ScheduleCandidate> blocks =
                    scheduledBlocks.stream()
                            .filter(block ->
                                    block.startTime()
                                            .isBefore(availableEnd)
                                            && block.endTime()
                                            .isAfter(
                                                    availability.startTime()
                                            )
                            )
                            .sorted(
                                    Comparator.comparing(
                                            ScheduleCandidate::startTime
                                    )
                            )
                            .toList();

            LocalDateTime cursor =
                    availability.startTime();

            for (ScheduleCandidate block : blocks) {

                LocalDateTime blockStart =
                        block.startTime().isBefore(availableEnd)
                                ? block.startTime()
                                : availableEnd;

                if (blockStart.isAfter(cursor)) {
                    availableMinutes +=
                            Duration.between(
                                    cursor,
                                    blockStart
                            ).toMinutes();
                }

                if (block.endTime().isAfter(cursor)) {
                    cursor = block.endTime();
                }

                if (!cursor.isBefore(availableEnd)) {
                    break;
                }
            }

            if (cursor.isBefore(availableEnd)) {
                availableMinutes +=
                        Duration.between(
                                cursor,
                                availableEnd
                        ).toMinutes();
            }
        }

        return availableMinutes < remainingMinutes;
    }
}