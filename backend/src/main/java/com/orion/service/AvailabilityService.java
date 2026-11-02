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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
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

        LocalTime startTime = request.startTime();
        LocalTime endTime = request.endTime();

        List<Availability> existingAvailability = availabilityRepository.findByUserIdAndDayOfWeek(
                userId, 
                request.dayOfWeek()
        );

        List<Availability> availabilityToMerge = 
                findMergeCandidates(
                        existingAvailability,
                        startTime, 
                        endTime,
                        null
                );

        for (Availability existing : availabilityToMerge) {
                if (existing.getStartTime().isBefore(startTime)) {
                        startTime = existing.getStartTime();
                }

                if (existing.getEndTime().isAfter(endTime)) {
                        endTime = existing.getEndTime();
                }
        }

        deleteAvailability(availabilityToMerge);

        Availability availability = new Availability(
                userId,
                request.dayOfWeek(),
                startTime, 
                endTime
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

        LocalTime startTime = request.startTime();
        LocalTime endTime = request.endTime();

        List<Availability> existingAvailability = 
                availabilityRepository.findByUserIdAndDayOfWeek(
                        userId,
                        request.dayOfWeek()
                );

        List<Availability> availabilityToMerge = 
                findMergeCandidates(
                        existingAvailability, 
                        startTime, 
                        endTime, 
                        availabilityId
                );

        for (Availability existing : availabilityToMerge) {
                if (existing.getStartTime().isBefore(startTime)) {
                        startTime = existing.getStartTime();
                }

                if (existing.getEndTime().isAfter(endTime)) {
                        endTime = existing.getEndTime();
                }
        }

        deleteAvailability(availabilityToMerge);

        availability.update(
                request.dayOfWeek(),
                startTime, 
                endTime
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
        if (startTime == null || endTime == null) {
                throw new InvalidAvailabilityException(
                        "Start time and end time must be provided"
                );
        }

        if (!startTime.isBefore(endTime)) {
            throw new InvalidAvailabilityException(
                    "Start time must be before end time"
            );
        }
    }

    private List<Availability> findMergeCandidates(
        List<Availability> existingAvailability,
        LocalTime startTime,
        LocalTime endTime,
        UUID availabilityIdToIgnore
    ) {
        List<Availability> candidates = new ArrayList<>();

        Set<UUID> selectedIds = new HashSet<>();

        LocalTime mergedStartTime = startTime;
        LocalTime mergedEndTime = endTime;

        boolean foundNewCandidate;

        do {
                foundNewCandidate = false;

                for (Availability existing : existingAvailability) {

                        if (availabilityIdToIgnore != null
                                        && existing.getId().equals(availabilityIdToIgnore)) {
                                continue;
                        }

                        if (selectedIds.contains(existing.getId())) {
                                continue;
                        }

                        boolean overlaps = !existing.getStartTime().isAfter(mergedEndTime)
                                        && !existing.getEndTime().isBefore(mergedStartTime);
                        
                        if (!overlaps) {
                                continue;
                        }

                        candidates.add(existing);
                        selectedIds.add(existing.getId());

                        if (existing.getStartTime().isBefore(mergedStartTime)) {
                                mergedStartTime = existing.getStartTime();
                        }

                        if (existing.getEndTime().isAfter(mergedEndTime)) {
                                mergedEndTime = existing.getEndTime();
                        }

                        foundNewCandidate = true;
                }
        } while (foundNewCandidate);

        return candidates;
    }

    private void deleteAvailability(
        List<Availability> availabilityToDelete
    ) {
        if (availabilityToDelete.isEmpty()) {
                return;
        }

        availabilityRepository.deleteAll(availabilityToDelete);

        availabilityRepository.flush();
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