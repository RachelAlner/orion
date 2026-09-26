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
    void initialiseRemainingMinutesFromEstimatedMinutes() {
        assertEquals(
                120, 
                task.getEstimatedMinutes()
        );

        assertEquals(
                120, 
                task.getRemainingMinutes()
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
        task.recordProgress(30);

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
        task.recordProgress(120);

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
    void doesNotAllowRemainingMinutesToBecomeNegative() {
        task.recordProgress(150);

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
    void rejectsZeroMinutesOfProgress() {
        assertThrows(
                IllegalArgumentException.class, 
                () -> task.recordProgress(0)
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
    void rejectsNegativeMinutesOfProgress() {
        assertThrows(
                IllegalArgumentException.class, 
                () -> task.recordProgress(-30)
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
    void rejectsProgressOnCompletedTask() {
        task.complete();

        assertThrows(
                IllegalStateException.class,
                () -> task.recordProgress(30)
        );

        assertEquals(
                0, 
                task.getRemainingMinutes()
        );

        assertEquals(
                TaskStatus.COMPLETED, 
                task.getStatus()
        );
    }

    @Test 
    void completeSetsRemainingMinutesToZero() {
        task.complete();

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

        task.recordProgress(60);

        assertEquals(
                60, 
                task.getRemainingMinutes()
        );

        assertEquals(
                TaskStatus.IN_PROGRESS,
                task.getStatus()
        );
    }
}