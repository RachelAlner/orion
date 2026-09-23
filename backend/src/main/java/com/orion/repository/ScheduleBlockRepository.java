package com.orion.repository;

import com.orion.model.Schedule;
import com.orion.model.ScheduleBlock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ScheduleBlockRepository
        extends JpaRepository<ScheduleBlock, UUID> {
    List<ScheduleBlock> findAllByScheduleOrderByStartTime(
            Schedule schedule
    );
}