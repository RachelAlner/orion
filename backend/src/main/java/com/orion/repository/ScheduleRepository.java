package com.orion.repository;

import com.orion.model.Schedule;
import com.orion.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

public interface ScheduleRepository
        extends JpaRepository<Schedule, UUID> {
    List<Schedule> findAllByUserIdOrderByGeneratedAtDesc(
            UUID userId
    );

    @EntityGraph(attributePaths = {
                "blocks",
                "blocks.task"
    })
    Optional<Schedule> findFirstByUserIdOrderByGeneratedAtDesc(
            UUID userId
    );
}