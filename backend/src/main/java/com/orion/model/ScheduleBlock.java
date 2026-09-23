package com.orion.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "schedule_blocks")
public class ScheduleBlock {

    @Id 
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "schedule_id",
            nullable = false
    )
    private Schedule schedule;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "task_id",
            nullable = false
    )
    private Task task;

    @Column(
            name = "start_time",
            nullable = false
    )
    private LocalDateTime startTime;

    @Column(
            name = "end_time",
            nullable = false
    )
    private LocalDateTime endTime;

    protected ScheduleBlock(){

    }

    public ScheduleBlock(
            Task task, 
            LocalDateTime startTime, 
            LocalDateTime endTime
    ) {
        this.id = UUID.randomUUID();
        this.task = task;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public UUID getId() {
        return id;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public Task getTask() {
        return task;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }
}

