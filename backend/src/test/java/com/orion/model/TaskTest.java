package com.orion.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    private UUID projectId;
    private Task task;

    @BeforeEach
    void setUp() {
        projectId = UUID.randomUUID();

        task = new Task(
            projectId, 
            "Test task",
            "Test description",
            120, 
            null, 
            2
        );
    }

    @Test 
    void newTaskStartsAsTodo() {
        assertEquals(
                TaskStatus.TODO, 
                task.getStatus()
        );
    }

    @Test 
    void recordsProgressAndUpdatesRemainingMinutes() {
        task.updateProgress(30);

        assertEquals(
                30, 
                task.getWorkedMinutes()
        );
        assertEquals(
                90, 
                task.getRemainingMinutes()
        );

        assertEquals(
            TaskStatus.IN_PROGRESS, 
            task.getStatus()
        );
    }

    @Test 
    void recordsProgressUntilTaskIsCompleted() {
        task.updateProgress(120);

        assertEquals(
                0, 
                task.getRemainingMinutes()
        );

        assertEquals(
                TaskStatus.COMPLETED,
                task.getStatus()
        );

        assertNotNull(task.getCompletedAt());
    }

    @Test 
    void workedMinutesBeyondEstimateKeepsRemainingAtZero() {
        task.updateProgress(150);

        assertEquals(
            0, 
            task.getRemainingMinutes()
        );

        assertEquals(
            TaskStatus.COMPLETED,
            task.getStatus()
        );

        assertNotNull(task.getCompletedAt());
    }

    @Test 
    void allowsZeroMinutesWorked() {
        task.updateProgress(0);

        assertEquals(0, task.getWorkedMinutes());

        assertEquals(
                120, 
                task.getRemainingMinutes()
        );

        assertEquals(
                TaskStatus.TODO, 
                task.getStatus()
        );

    }

    @Test 
    void rejectsNegativeMinutesOfProgress() {
        assertThrows(
                IllegalArgumentException.class, 
                () -> task.updateProgress(-30)
        );

        assertEquals(
                120, 
                task.getRemainingMinutes()
        );

        assertEquals(
                TaskStatus.TODO, 
                task.getStatus()
        );
    }

    @Test 
    void completeSetsRemainingMinutesToZero() {
        task.complete();

        assertEquals(
                120, 
                task.getWorkedMinutes()
        );

        assertEquals(
                0, 
                task.getRemainingMinutes()
        );

        assertEquals(
                TaskStatus.COMPLETED,
                task.getStatus()
        );

        assertNotNull(task.getCompletedAt());
    }

    @Test 
    void recordProgressChangesTodoToInProgress() {
        assertEquals(
                TaskStatus.TODO, 
                task.getStatus()
        );

        task.updateProgress(60);

        assertEquals(
                60, 
                task.getRemainingMinutes()
        );

        assertEquals(
                TaskStatus.IN_PROGRESS,
                task.getStatus()
        );
    }

    @Test 
    void updatePreservesWorkedMinutes() {
        Task task = new Task(
                projectId, 
                "Original task",
                null, 
                120, 
                null, 
                3
        );

        task.updateProgress(50);

        task.update(
                "Updated task",
                "Updated description",
                120,
                null, 
                4
        );

        assertEquals(120, task.getEstimatedMinutes());
        assertEquals(70, task.getRemainingMinutes());
        assertEquals("Updated task", task.getTitle());
        assertEquals(4, task.getPriority());
    }

    @Test 
    void updateRecalculatedRemainingUsingWorkedMinutes() {
        Task task = new Task(
                projectId, 
                "Task",
                null, 
                120, 
                null, 
                3
        );

        task.updateProgress(50);

        task.update(
                "Task",
                null, 
                180,
                null, 
                3
        );

        assertEquals(180, task.getEstimatedMinutes());
        assertEquals(130, task.getRemainingMinutes());
    }

    @Test 
    void updateRemovesRemainingTimeWhenEstimateIsRemoved() {
        Task task = new Task(
                projectId, 
                "Task", 
                null, 
                120, 
                null, 
                3
        );

        task.updateProgress(50);

        task.update(
                "Task",
                null, 
                null, 
                null, 
                3
        );

        assertNull(task.getEstimatedMinutes());
        assertNull(task.getRemainingMinutes());
    }

    @Test 
    void updateReopensCompletedTaskWhenRemainingMinutesBecomePositive() {
        Task task = new Task(
                projectId, 
                "Task",
                null, 
                120, 
                null, 
                3
        );

        task.complete();

        assertEquals(TaskStatus.COMPLETED, task.getStatus());
        assertEquals(0, task.getRemainingMinutes());

        task.update(
                "Task",
                null, 
                180, 
                null, 
                3
        );

        assertEquals(180, task.getEstimatedMinutes());
        assertEquals(60, task.getRemainingMinutes());
        assertEquals(
                TaskStatus.IN_PROGRESS,
                task.getStatus()
        );
        assertNull(task.getCompletedAt());
    }

    @Test 
    void updateKeepsCompletedStatusWhenRemainingMinutesAreZero() {
        Task task = new Task(
                projectId, 
                "Task",
                null, 
                120, 
                null, 
                3
        );

        task.complete();

        task.update(
                "Updated task",
                null, 
                120, 
                null, 
                3
        );

        assertEquals(0, task.getRemainingMinutes());
        assertEquals(
                TaskStatus.COMPLETED,
                task.getStatus()
        );
    }

    @Test 
    void newTaskStartsWithZeroWorkedMinutes() {
        Task task = new Task(
                projectId, 
                "Test task",
                "Description",
                120, 
                null, 
                3
        );

        assertEquals(0, task.getWorkedMinutes());
        assertEquals(120, task.getRemainingMinutes());
        assertEquals(TaskStatus.TODO, task.getStatus());
    }

    @Test 
    void updateProgressReplacesExistingWorkedMinutes() {
        Task task = new Task(
                projectId, 
                "Test task",
                "Description",
                120, 
                null, 
                3
        );

        task.updateProgress(60);
        task.updateProgress(40);

        assertEquals(40, task.getWorkedMinutes());
        assertEquals(80, task.getRemainingMinutes());
        assertEquals(TaskStatus.IN_PROGRESS, task.getStatus());
    }

    @Test 
    void updateProgressBeyondEstimateKeepsRemainingAtZero() {
        Task task = new Task(
                projectId, 
                "Test task",
                "Description",
                120, 
                null, 
                3
        );

        task.updateProgress(150);

        assertEquals(150, task.getWorkedMinutes());
        assertEquals(0, task.getRemainingMinutes());
        assertEquals(TaskStatus.COMPLETED, task.getStatus());
    }

    @Test 
    void increasingEstimateReopensCompletedTask() {
        task.updateProgress(120);

        assertEquals(TaskStatus.COMPLETED, task.getStatus());

        task.update(
                "Test task",
                "Description",
                180,
                null, 
                3
        );

        assertEquals(120, task.getWorkedMinutes());
        assertEquals(60, task.getRemainingMinutes());
        assertEquals(TaskStatus.IN_PROGRESS, task.getStatus());
        assertNull(task.getCompletedAt());
    }

    @Test 
    void updateCompletesTaskWhenEstimateIsLessThanWorkedMinutes() {
        task.updateProgress(100);

        assertEquals(TaskStatus.IN_PROGRESS, task.getStatus());

        task.update(
                "Test task",
                "Test description",
                80, 
                null, 
                2
        );

        assertEquals(80, task.getEstimatedMinutes());
        assertEquals(100, task.getWorkedMinutes());
        assertEquals(0, task.getRemainingMinutes());
        assertEquals(TaskStatus.COMPLETED, task.getStatus());
        assertNotNull(task.getCompletedAt());
    }
}