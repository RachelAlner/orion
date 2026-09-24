package com.orion.service;

import com.orion.model.Schedule;
import com.orion.model.ScheduleBlock;
import com.orion.model.User;
import com.orion.model.Task;
import com.orion.model.Project;
import com.orion.model.Availability;
import com.orion.repository.ScheduleRepository;
import com.orion.repository.TaskRepository;
import com.orion.repository.AvailabilityRepository;
import com.orion.repository.ProjectRepository;
import com.orion.repository.TaskDependencyRepository;
import com.orion.scheduler.*;
import com.orion.scheduler.SchedulerInput;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service 
@Transactional
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final TaskDependencyRepository dependencyRepository;
    private final AvailabilityRepository availabilityRepository;
    private final Scheduler scheduler;

    public ScheduleService(
            ScheduleRepository scheduleRepository,
            ProjectRepository projectRepository,
            TaskRepository taskRepository, 
            TaskDependencyRepository dependencyRepository,
            AvailabilityRepository availabilityRepository,
            Scheduler scheduler
    ) {
        this.scheduleRepository = scheduleRepository;
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.dependencyRepository = dependencyRepository;
        this.availabilityRepository = availabilityRepository;
        this.scheduler = scheduler;
    }

    public ScheduleResult generateSchedule(
            User user, 
            LocalDateTime periodStart, 
            LocalDateTime periodEnd
    ) {
        validatePeriod(periodStart, periodEnd);

        List<Task> taskEntities = 
                getAllUserTasks(user);

        List<SchedulingTask> tasks = 
                loadSchedulingTasks(taskEntities);
        
        List<TaskDependency> dependencies = 
                loadDependencies(taskEntities);
        
        List<AvailabilityWindow> availabilityWindows = 
                loadAvailabilityWindows(
                        user, 
                        periodStart, 
                        periodEnd
                );
        
        List<ExistingScheduleBlock> existingScheduleBlocks = 
                loadExistingScheduleBlocks(user);
        
        SchedulerInput input = new SchedulerInput(
                tasks, 
                dependencies, 
                availabilityWindows, 
                existingScheduleBlocks, 
                periodStart, 
                periodEnd
        );

        ScheduleResult result = 
                scheduler.generate(input);
        
        persistSchedule(
                user, 
                periodStart, 
                periodEnd, 
                result, 
                taskEntities
        );

        return result;
    }

    private void validatePeriod(
            LocalDateTime periodStart, 
            LocalDateTime periodEnd
    ) {
        if (periodStart == null || periodEnd == null) {
            throw new IllegalArgumentException(
                    "Scheduling period must be provided"
            );
        }

        if (!periodStart.isBefore(periodEnd)) {
            throw new IllegalArgumentException(
                    "Scheduling period start must be before period end"
            );
        }
    } 

    private List<Task> getAllUserTasks(User user) {
        List<Project> projects = projectRepository.findByUserId(user.getId());
        List<Task> allUserTasks = new ArrayList<>();

        for (Project project : projects) {
            allUserTasks.addAll(taskRepository.findByProjectId(project.getId()));
        }

        return allUserTasks;
    } 

    private List<SchedulingTask> loadSchedulingTasks(List<Task> taskEntities) {
        return taskEntities
                .stream()
                .map(task -> {
                    LocalDateTime createdAt = task.getCreatedAt().toLocalDateTime();
                    return new SchedulingTask(
                            task.getId(),
                            task.getEstimatedMinutes() != null ? task.getEstimatedMinutes() : 0,
                            task.getEstimatedMinutes() != null ? task.getEstimatedMinutes() : 0,
                            task.getDeadline(),
                            task.getPriority() != null ? task.getPriority() : 0,
                            task.getStatus(),
                            createdAt
                    );
                })
                .toList();
    }

    private List<TaskDependency> loadDependencies(List<Task> taskEntities) {
        List<TaskDependency> allTaskDependencies = new ArrayList<>();

        for (Task task : taskEntities) {
            List<com.orion.model.TaskDependency> entityDependencies =
                    dependencyRepository.findByIdTaskId(task.getId());

            for (com.orion.model.TaskDependency dependency : entityDependencies) {
                allTaskDependencies.add(new TaskDependency(
                        dependency.getId().getTaskId(),
                        dependency.getId().getDependsOnTaskId()
                ));
            }
        }

        return allTaskDependencies;
    }

    private List<AvailabilityWindow> loadAvailabilityWindows(
            User user,
            LocalDateTime periodStart,
            LocalDateTime periodEnd
    ) {
        List<Availability> availabilityPatterns = 
                availabilityRepository.findByUserIdOrderByDayOfWeekAscStartTimeAsc(user.getId());

        List<AvailabilityWindow> windows = new ArrayList<>();
        LocalDateTime currentDay = periodStart; 

        while (!currentDay.isAfter(periodEnd)) { 
            DayOfWeek dayOfWeek = currentDay.getDayOfWeek();

            for (Availability pattern : availabilityPatterns) {
                if (pattern.getDayOfWeek() == dayOfWeek) {
                    LocalDateTime windowStart = currentDay.with(pattern.getStartTime());
                    LocalDateTime windowEnd = currentDay.with(pattern.getEndTime());

                    if (windowStart.isBefore(periodEnd) && windowEnd.isAfter(periodStart)) {
                        LocalDateTime actualStart = windowStart.isBefore(periodStart) ? periodStart : windowStart;
                        LocalDateTime actualEnd = windowEnd.isAfter(periodEnd) ? periodEnd : windowEnd;

                        if (actualStart.isBefore(actualEnd)) {
                            windows.add(new AvailabilityWindow(actualStart, actualEnd));
                        }
                    }
                }
            }

            currentDay = currentDay.plusDays(1);
        }

        return windows;
    }

    private List<ExistingScheduleBlock> loadExistingScheduleBlocks(
            User user
    ) {
        List<Schedule> schedules = 
                scheduleRepository
                        .findAllByUserOrderByGeneratedAtDesc(user);
        
        if (schedules.isEmpty()) {
            return List.of();
        }

        Schedule latestSchedule = schedules.get(0);

        return latestSchedule.getBlocks()
                .stream()
                .map(block -> new ExistingScheduleBlock(
                        block.getTask().getId(),
                        block.getStartTime(), 
                        block.getEndTime()
                ))
                .toList();
    }

    private Schedule persistSchedule(
            User user, 
            LocalDateTime periodStart, 
            LocalDateTime periodEnd, 
            ScheduleResult result, 
            List<Task> tasks
    ) {
        Map<UUID, Task> tasksById = 
                tasks.stream()
                        .collect(Collectors.toMap(
                                Task::getId,
                                task -> task
                        ));

        Schedule schedule = new Schedule(
                user, 
                periodStart, 
                periodEnd, 
                LocalDateTime.now()
        );

        for (ScheduleCandidate candidate : result.scheduledBlocks()) {

            Task task = tasksById.get(candidate.taskId());

            if (task == null) {
                throw new IllegalStateException(
                        "Scheduled task does not belong to user"
                );
            }
            
            ScheduleBlock block = new ScheduleBlock(
                    task, 
                    candidate.startTime(),
                    candidate.endTime()
            );

            schedule.addBlock(block);
        }

        return scheduleRepository.save(schedule);
    }

}