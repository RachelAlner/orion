package com.orion.repository;

import com.orion.model.Availability;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AvailabilityRepository extends JpaRepository<Availability, UUID> {

    List<Availability> findByUserIdOrderByDayOfWeekAscStartTimeAsc(UUID userId);

    List<Availability> findByUserIdAndDayOfWeek(
            UUID userId, 
            DayOfWeek dayOfWeek
    );

    Optional<Availability> findByIdAndUserId(
            UUID id, 
            UUID userId
    );
}