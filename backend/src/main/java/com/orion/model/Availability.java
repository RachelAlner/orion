package com.orion.model;

import jakarta.persistence.*;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "availability")
public class Availability {

    @Id 
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Convert(converter = DayOfWeekConverter.class)
    @Column(name = "day_of_week", nullable = false)
    private DayOfWeek dayOfWeek;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    protected Availability() {

    }

    public Availability(
            UUID userId, 
            DayOfWeek dayOfWeek,
            LocalTime startTime,
            LocalTime endTime
    ) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void update(
            DayOfWeek dayOfWeek,
            LocalTime startTime,
            LocalTime endTime
    ) {
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}