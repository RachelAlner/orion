package com.orion.repository;

import com.orion.model.Schedule;
import com.orion.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ScheduleRepository
        extends JpaRepository<Schedule, UUID> {
    List<Schedule> findAllByUserOrderByGeneratedAtDesc(
            User user
    );
}