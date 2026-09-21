package com.orion.service;

import com.orion.dto.AvailabilityResponse;
import com.orion.dto.CreateAvailabilityRequest;
import com.orion.dto.UpdateAvailabilityRequest;
import com.orion.exception.AvailabilityNotFoundException;
import com.orion.exception.InvalidAvailabilityException;
import com.orion.model.Availability;
import com.orion.repository.AvailabilityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class AvailabilityService {

    private final AvailabilityRepository availabilityRepository;

    public AvailabilityService(AvailabilityRepository availabilityRepository) {
        this.availabilityRepository = availabilityRepository;
    }

    public AvailabilityResponse create(
            UUID userId, 
            CreateAvailabilityRequest request
    ) {
        validateTimeRange(
                request.startTime(),
                request.endTime()
        );

        validateNoOverlap(
                userId, 
                request.dayOfWeek(),
                request.startTime(),
                request.endTime(), 
                null
        );

        Availability availability = new Availability(
                userId, 
                request.dayOfWeek(),
                request.startTime(), 
                request.endTime()
        );

        Availability saved = availabilityRepository.save(availability);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<AvailabilityResponse> findAll(UUID userId) {
        return availabilityRepository
                .findByUserIdOrderByDayOfWeekAscStartTimeAsc(userId) 
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public AvailabilityResponse update(
            UUID userId, 
            UUID availabilityId, 
            UpdateAvailabilityRequest request
    ) {
        Availability availability = availabilityRepository
                .findByIdAndUserId(availabilityId, userId)
                .orElseThrow(AvailabilityNotFoundException::new);
        
        validateTimeRange(
                request.startTime(),
                request.endTime()
        );

        validateNoOverlap(
                userId, 
                request.dayOfWeek(),
                request.startTime(),
                request.endTime(),
                availabilityId
        );

        availability.update(
                request.dayOfWeek(),
                request.startTime(),
                request.endTime()
        );

        Availability saved = availabilityRepository.save(availability);

        return toResponse(saved);
    }

    public void delete(
            UUID userId, 
            UUID availabilityId
    ) {
        Availability availability = availabilityRepository
                .findByIdAndUserId(availabilityId, userId)
                .orElseThrow(AvailabilityNotFoundException::new);

        availabilityRepository.delete(availability);
    }

    private void validateTimeRange(
            LocalTime startTime,
            LocalTime endTime
    ) {
        if (!startTime.isBefore(endTime)) {
            throw new InvalidAvailabilityException(
                    "Start time must be before end time"
            );
        }
    }

    private void validateNoOverlap(
            UUID userId, 
            DayOfWeek dayOfWeek, 
            LocalTime startTime, 
            LocalTime endTime, 
            UUID availabilityIdToIgnore
    ) {
        List<Availability> existingAvailability = 
                availabilityRepository.findByUserIdAndDayOfWeek(
                        userId,
                        dayOfWeek
                );

        boolean overlaps = existingAvailability.stream()
                .filter(existing -> 
                        availabilityIdToIgnore == null 
                                || !existing.getId().equals(availabilityIdToIgnore)
                )
                .anyMatch(existing -> 
                        startTime.isBefore(existing.getEndTime())
                                && endTime.isAfter(existing.getStartTime())
                );
        
        if (overlaps) {
            throw new InvalidAvailabilityException(
                    "Availability periods must not overlap"
            );
        }
    }

    private AvailabilityResponse toResponse(Availability availability) {
        return new AvailabilityResponse(
                availability.getId(), 
                availability.getDayOfWeek(),
                availability.getStartTime(),
                availability.getEndTime()
        );
    }
}