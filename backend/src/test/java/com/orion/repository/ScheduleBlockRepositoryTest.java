package com.orion.repository;

import com.orion.model.Schedule;
import com.orion.model.ScheduleBlock;
import com.orion.model.Project;
import com.orion.model.Task;
import com.orion.model.TaskStatus;
import com.orion.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ScheduleBlockRepositoryTest {
    @Autowired 
    private ScheduleBlockRepository scheduleBlockRepository;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired 
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Test 
    void savesAndFindsScheduleBlock() {
        User user = new User(
                "block-test@example.com",
                "hashed-password"
        );

        user = userRepository.save(user);
        
        Project project = new Project(
                user.getId(), 
                "Project",
                "Project description",
                LocalDateTime.of(2026, 10, 1, 0, 0),
                2
        );

        project = projectRepository.save(project);

        Task task = new Task(
                project.getId(), 
                "Implement authentication",
                "Finish JWT authentication",
                120, 
                LocalDateTime.of(2026, 9, 25, 0, 0),
                2
        );

        task = taskRepository.save(task);

        Schedule schedule = new Schedule(
                user, 
                LocalDateTime.of(2026, 9, 21, 0, 0),
                LocalDateTime.of(2026, 9, 28, 0, 0),
                LocalDateTime.of(2026, 9, 21, 9, 0)
        );

        schedule = scheduleRepository.save(schedule);

        LocalDateTime startTime = 
                LocalDateTime.of(2026, 9, 21, 9, 0);
        
        LocalDateTime endTime = 
                LocalDateTime.of(2026, 9, 21, 11, 0);

        ScheduleBlock block = new ScheduleBlock(
                task, 
                startTime, 
                endTime
        );

        schedule.addBlock(block);

        scheduleRepository.save(schedule);

        ScheduleBlock savedBlock = 
                scheduleBlockRepository
                        .findAllByScheduleOrderByStartTime(schedule)
                        .get(0);
        
        assertNotNull(savedBlock.getId());
        assertEquals(savedBlock.getSchedule().getId(), schedule.getId());
        assertEquals(savedBlock.getTask().getId(), task.getId());
        assertEquals(savedBlock.getStartTime(), startTime);
        assertEquals(savedBlock.getEndTime(), endTime);
    }

    @Test 
    void findsBlocksForScheduleOrderedByStartTime() {
        User user = new User(
                "block-order-test@example.com",
                "hashed-password"
        );

        user = userRepository.save(user);

        Project project = new Project(
                user.getId(), 
                "Project",
                "Project description",
                LocalDateTime.of(2026, 10, 1, 0, 0),
                2
        );

        project = projectRepository.save(project);

        Task firstTask = new Task(
                project.getId(),
                "First task", 
                "First scheduled task",
                60, 
                LocalDateTime.of(2026, 9, 25, 0, 0),
                1
        );

        Task secondTask = new Task(
                project.getId(),
                "Second task", 
                "Second scheduled task",
                60, 
                LocalDateTime.of(2026, 9, 25, 0, 0),
                1
        );

        firstTask = taskRepository.save(firstTask);
        secondTask = taskRepository.save(secondTask);

        Schedule schedule = new Schedule(
                user, 
                LocalDateTime.of(2026, 9, 21, 0, 0),
                LocalDateTime.of(2026, 9, 28, 0, 0),
                LocalDateTime.of(2026, 9, 21, 9, 0)
        );

        schedule = scheduleRepository.save(schedule);

        ScheduleBlock laterBlock = new ScheduleBlock(
                secondTask, 
                LocalDateTime.of(2026, 9, 21, 14, 0), 
                LocalDateTime.of(2026, 9, 21, 15, 0)
        );

        ScheduleBlock earlierBlock = new ScheduleBlock(
                firstTask, 
                LocalDateTime.of(2026, 9, 21, 9, 0), 
                LocalDateTime.of(2026, 9, 21, 10, 0)
        );

        schedule.addBlock(laterBlock);
        schedule.addBlock(earlierBlock);

        scheduleRepository.save(schedule);

        List<ScheduleBlock> blocks = 
                scheduleBlockRepository.findAllByScheduleOrderByStartTime(schedule);
        
        assertEquals(2, blocks.size());
        assertEquals(blocks.get(0).getStartTime(), earlierBlock.getStartTime());
        assertEquals(blocks.get(0).getEndTime(), earlierBlock.getEndTime());
        assertEquals(blocks.get(1).getStartTime(), laterBlock.getStartTime());
        assertEquals(blocks.get(1).getEndTime(), laterBlock.getEndTime());

    }

    @Test 
    void onlyFindsBlocksBelongingToSpecifiedSchedule() {
        User user = new User(
                "block-schedule-test@example.com",
                "hashed-password"
        );

        user = userRepository.save(user);

        Project project = new Project(
                user.getId(), 
                "Project",
                "Project description",
                LocalDateTime.of(2026, 10, 1, 0, 0),
                2
        );

        project = projectRepository.save(project);

        Task firstTask = new Task(
                project.getId(), 
                "First task",
                "Task for first schedule",
                60,
                LocalDateTime.of(2026, 9, 25, 0, 0),
                1
        );

        Task secondTask = new Task(
                project.getId(), 
                "Second task",
                "Task for second schedule",
                60,
                LocalDateTime.of(2026, 9, 25, 0, 0),
                1
        );

        firstTask = taskRepository.save(firstTask);
        secondTask = taskRepository.save(secondTask);

        Schedule firstSchedule = new Schedule(
                user, 
                LocalDateTime.of(2026, 9, 21, 0, 0),
                LocalDateTime.of(2026, 9, 28, 0, 0),
                LocalDateTime.of(2026, 9, 21, 9, 0)
        );

        Schedule secondSchedule = new Schedule(
                user, 
                LocalDateTime.of(2026, 9, 28, 0, 0),
                LocalDateTime.of(2026, 10, 5, 0, 0),
                LocalDateTime.of(2026, 9, 28, 9, 0)
        );

        firstSchedule = scheduleRepository.save(firstSchedule);
        secondSchedule = scheduleRepository.save(secondSchedule);

        ScheduleBlock firstBlock = new ScheduleBlock(
                firstTask, 
                LocalDateTime.of(2026, 9, 21, 9, 0), 
                LocalDateTime.of(2026, 9, 21, 10, 0)
        );

        ScheduleBlock secondBlock = new ScheduleBlock(
                secondTask, 
                LocalDateTime.of(2026, 9, 28, 9, 0), 
                LocalDateTime.of(2026, 9, 28, 10, 0)
        );

        firstSchedule.addBlock(firstBlock);
        secondSchedule.addBlock(secondBlock);

        scheduleRepository.save(firstSchedule);
        scheduleRepository.save(secondSchedule);

        List<ScheduleBlock> blocks = 
                scheduleBlockRepository.findAllByScheduleOrderByStartTime(firstSchedule);
        
        assertEquals(1, blocks.size());
        assertEquals(blocks.get(0).getStartTime(), firstBlock.getStartTime());
        assertEquals(blocks.get(0).getEndTime(), firstBlock.getEndTime());
        assertEquals(blocks.get(0).getSchedule().getId(), firstSchedule.getId());

    }


}