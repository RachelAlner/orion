package com.orion.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "schedules")
public class Schedule {

    @Id 
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id", 
            nullable = false
    )
    private User user;

    @Column(
            name = "period_start",
            nullable = false
    )
    private LocalDateTime periodStart;

    @Column(
            name = "period_end",
            nullable = false
    )
    private LocalDateTime periodEnd;

    @Column(
            name = "generated_at",
            nullable = false
    )
    private LocalDateTime generatedAt;

    @OneToMany(
            mappedBy = "schedule",
            cascade = CascadeType.ALL, 
            orphanRemoval = true
    )
    private List<ScheduleBlock> blocks = new ArrayList<>();

    protected Schedule() {}

    public Schedule(
            User user, 
            LocalDateTime periodStart, 
            LocalDateTime periodEnd, 
            LocalDateTime generatedAt
    ) {
        id = UUID.randomUUID();
        this.user = user;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.generatedAt = generatedAt;
    }

    public UUID getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public LocalDateTime getPeriodStart() {
        return periodStart;
    }

    public LocalDateTime getPeriodEnd() {
        return periodEnd;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public List<ScheduleBlock> getBlocks() {
        return blocks; 
    }

    public void addBlock(ScheduleBlock block) {
        blocks.add(block);
        block.setSchedule(this);
    }

    public void removeBlock(ScheduleBlock block) {
        blocks.remove(block);
        block.setSchedule(null);
    }

}