package com.orion.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TaskDependencyTest {

    @Test 
    void shouldCreateTaskDependencyId() {
        UUID taskId = UUID.randomUUID();
        UUID dependsOnTaskId = UUID.randomUUID();

        TaskDependencyId id = 
                new TaskDependencyId(
                        taskId, 
                        dependsOnTaskId
                );
        
        assertEquals(
                taskId, 
                id.getTaskId()
        );

        assertEquals(
                dependsOnTaskId, 
                id.getDependsOnTaskId()
        );

                
    }

    @Test 
    void shouldTreatIdsWithSameValueAsEqual() {
        UUID taskId = UUID.randomUUID();
        UUID dependsOnTaskId = UUID.randomUUID();

        TaskDependencyId first = 
                new TaskDependencyId(
                        taskId, 
                        dependsOnTaskId
                );
        
        TaskDependencyId second = 
                new TaskDependencyId(
                        taskId, 
                        dependsOnTaskId
                );

        assertEquals(first, second);
    }

    @Test 
    void shouldHaveSameHashCodeForEqualIds() {
        UUID taskId = UUID.randomUUID();
        UUID dependsOnTaskId = UUID.randomUUID();

        TaskDependencyId first = 
                new TaskDependencyId(
                        taskId, 
                        dependsOnTaskId
                );
        
        TaskDependencyId second = 
                new TaskDependencyId(
                        taskId, 
                        dependsOnTaskId
                );

        assertEquals(
                first.hashCode(), 
                second.hashCode()
        );

    }

    @Test 
    void shouldNotTeatIdsWithDifferentTaskIdsAsEqual() {
        UUID taskId1 = UUID.randomUUID();
        UUID taskId2 = UUID.randomUUID();
        UUID dependsOnTaskId = UUID.randomUUID();

        TaskDependencyId first = 
                new TaskDependencyId(
                        taskId1, 
                        dependsOnTaskId
                );
        
        TaskDependencyId second = 
                new TaskDependencyId(
                        taskId2, 
                        dependsOnTaskId
                );

        assertNotEquals(first, second);
    }

    @Test 
    void shouldNotTreatIdsWithDifferentDependencyIdsAsEqual() {
        UUID taskId = UUID.randomUUID();
        UUID dependsOnTaskId1 = UUID.randomUUID();
        UUID dependsOnTaskId2 = UUID.randomUUID();

        TaskDependencyId first = 
                new TaskDependencyId(
                        taskId, 
                        dependsOnTaskId1
                );
        
        TaskDependencyId second = 
                new TaskDependencyId(
                        taskId, 
                        dependsOnTaskId2
                );

        assertNotEquals(first, second);
    }

    @Test 
    void shouldCreateTaskDependency() {
        UUID taskId = UUID.randomUUID();
        UUID dependsOnTaskId = UUID.randomUUID();

        TaskDependencyId id = 
                new TaskDependencyId(
                        taskId, 
                        dependsOnTaskId
                );
        
        TaskDependency dependency = new TaskDependency(id);

        assertNotNull(dependency.getId());

        assertEquals(
                id, 
                dependency.getId()
        );

        assertEquals(
                taskId, 
                dependency.getId().getTaskId()
        );

        assertEquals(
                dependsOnTaskId,
                dependency.getId().getDependsOnTaskId()
        );
    }
}