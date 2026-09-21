package com.orion.service;

import com.orion.dto.AvailabilityResponse;
import com.orion.dto.CreateAvailabilityRequest;
import com.orion.dto.UpdateAvailabilityRequest;
import com.orion.exception.AvailabilityNotFoundException;
import com.orion.exception.InvalidAvailabilityException;
import com.orion.model.Availability;
import com.orion.repository.AvailabilityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AvailabilityServiceTest {

    @Mock
    private AvailabilityRepository availabilityRepository;

    @InjectMocks
    private AvailabilityService availabilityService;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
    }

    @Test 
    void shouldCreateAvailability() {
        CreateAvailabilityRequest request = 
                new CreateAvailabilityRequest(
                        DayOfWeek.MONDAY,
                        LocalTime.of(9,0),
                        LocalTime.of(12,0)
                );
        
        when(availabilityRepository.findByUserIdAndDayOfWeek(
                userId,
                DayOfWeek.MONDAY
        )).thenReturn(List.of());

        when(availabilityRepository.save(any(Availability.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        
        AvailabilityResponse response = 
                availabilityService.create(userId, request);
        
        assertNotNull(response);
        assertEquals(response.dayOfWeek(), DayOfWeek.MONDAY);
        assertEquals(response.startTime(), LocalTime.of(9,0));
        assertEquals(response.endTime(), LocalTime.of(12, 0));

        verify(availabilityRepository).save(any(Availability.class));
    }

    @Test 
    void shouldRejectWhenStartTimeIsAfterEndTime() {
        CreateAvailabilityRequest request = 
                new CreateAvailabilityRequest(
                        DayOfWeek.MONDAY,
                        LocalTime.of(12,0),
                        LocalTime.of(9,0)
                );

        assertThatThrownBy(() -> 
                availabilityService.create(userId, request)
        )
                .isInstanceOf(InvalidAvailabilityException.class)
                .hasMessage("Start time must be before end time");
        
        verify(availabilityRepository, never())
                .findByUserIdAndDayOfWeek(any(), any());
        
        verify(availabilityRepository, never())
                .save(any());
    }

    @Test 
    void shouldRejectWhenStartTimeEqualsEndTime() {
        CreateAvailabilityRequest request = 
                new CreateAvailabilityRequest(
                        DayOfWeek.MONDAY,
                        LocalTime.of(9,0),
                        LocalTime.of(9,0)
                );

        assertThatThrownBy(() -> 
                availabilityService.create(userId, request)
        )
                .isInstanceOf(InvalidAvailabilityException.class)
                .hasMessage("Start time must be before end time");
        
        verify(availabilityRepository, never())
                .findByUserIdAndDayOfWeek(any(), any());
        
        verify(availabilityRepository, never())
                .save(any());
        
    }

    @Test 
    void shouldRejectOverlappingAvailability() {
        Availability existing = new Availability(
                userId, 
                DayOfWeek.MONDAY,
                LocalTime.of(9, 0),
                LocalTime.of(12, 0)
        );

        CreateAvailabilityRequest request = 
                new CreateAvailabilityRequest(
                        DayOfWeek.MONDAY,
                        LocalTime.of(11,0),
                        LocalTime.of(14,0)
                );
        
        when(availabilityRepository.findByUserIdAndDayOfWeek(
                userId, 
                DayOfWeek.MONDAY
        )).thenReturn(List.of(existing));

        assertThatThrownBy(() -> 
                availabilityService.create(userId, request)
        )
                .isInstanceOf(InvalidAvailabilityException.class)
                .hasMessage("Availability periods must not overlap");

        verify(availabilityRepository, never())
                .save(any());
    }

    @Test 
    void shouldAllowAdjacentAvailability() {
        Availability existing = new Availability(
                userId, 
                DayOfWeek.MONDAY,
                LocalTime.of(9, 0),
                LocalTime.of(12, 0)
        );

        CreateAvailabilityRequest request = 
                new CreateAvailabilityRequest(
                    DayOfWeek.MONDAY,
                    LocalTime.of(12, 0),
                    LocalTime.of(15, 0)
                );
        
        when(availabilityRepository.findByUserIdAndDayOfWeek(
                userId, 
                DayOfWeek.MONDAY
        )).thenReturn(List.of(existing));

        when(availabilityRepository.save(any(Availability.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        
        AvailabilityResponse response = 
                availabilityService.create(userId, request);
        
        assertEquals(response.startTime(), LocalTime.of(12, 0));
        assertEquals(response.endTime(), LocalTime.of(15, 0));

        verify(availabilityRepository).save(any(Availability.class));
    }

    @Test 
    void shouldAllowAvailabilityOnDifferentDay() {
        Availability existing = new Availability(
                userId, 
                DayOfWeek.MONDAY,
                LocalTime.of(9, 0),
                LocalTime.of(12, 0)
        );

        CreateAvailabilityRequest request = 
                new CreateAvailabilityRequest(
                        DayOfWeek.TUESDAY, 
                        LocalTime.of(9, 0), 
                        LocalTime.of(12, 0)
                );
        
        when(availabilityRepository.findByUserIdAndDayOfWeek(
                userId, 
                DayOfWeek.TUESDAY
        )).thenReturn(List.of());

        when(availabilityRepository.save(any(Availability.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AvailabilityResponse response = 
                availabilityService.create(userId, request);
        
        assertEquals(response.dayOfWeek(), DayOfWeek.TUESDAY);

        verify(availabilityRepository).save(any(Availability.class));
    }

    @Test 
    void shouldFindAllAvailabilityForUser() {
        Availability monday = new Availability(
                userId, 
                DayOfWeek.MONDAY, 
                LocalTime.of(9, 0),
                LocalTime.of(12, 0)
        );

        Availability tuesday = new Availability(
                userId, 
                DayOfWeek.TUESDAY, 
                LocalTime.of(10, 0),
                LocalTime.of(13, 0)
        );

        when(availabilityRepository
                .findByUserIdOrderByDayOfWeekAscStartTimeAsc(userId))
                .thenReturn(List.of(monday, tuesday));
        
        List<AvailabilityResponse> result = 
                availabilityService.findAll(userId);

        assertThat(result).hasSize(2);

        assertEquals(result.get(0).dayOfWeek(), DayOfWeek.MONDAY);

        assertEquals(result.get(1).dayOfWeek(), DayOfWeek.TUESDAY);

        verify(availabilityRepository)
                .findByUserIdOrderByDayOfWeekAscStartTimeAsc(userId);
    }

    @Test 
    void shouldUpdateAvailability() {
        Availability existing = new Availability(
                userId, 
                DayOfWeek.MONDAY, 
                LocalTime.of(9, 0),
                LocalTime.of(12, 0)
        );


        UUID availabilityId = existing.getId();

        UpdateAvailabilityRequest request = 
                new UpdateAvailabilityRequest(
                        DayOfWeek.MONDAY, 
                        LocalTime.of(10, 0),
                        LocalTime.of(13, 0)
                );
        
        when(availabilityRepository.findByIdAndUserId(
                availabilityId, 
                userId
        )).thenReturn(Optional.of(existing));

        when(availabilityRepository.findByUserIdAndDayOfWeek(
                userId, 
                DayOfWeek.MONDAY
        )).thenReturn(List.of(existing));

        when(availabilityRepository.save(any(Availability.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        
        AvailabilityResponse response = 
                availabilityService.update(
                        userId, 
                        availabilityId, 
                        request
                );

        assertEquals(response.dayOfWeek(), DayOfWeek.MONDAY);
        assertEquals(response.startTime(), LocalTime.of(10, 0));
        assertEquals(response.endTime(), LocalTime.of(13, 0));

        verify(availabilityRepository).save(existing);
    }

    @Test 
    void shouldRejectUpdateWhenAvailabilityDoesNotBelongToUser() {
        UUID availabilityId = UUID.randomUUID();
        
        UpdateAvailabilityRequest request = 
                new UpdateAvailabilityRequest(
                        DayOfWeek.MONDAY,
                        LocalTime.of(10, 0),
                        LocalTime.of(13, 0)
                );

        when(availabilityRepository.findByIdAndUserId(
                availabilityId, 
                userId
        )).thenReturn(Optional.empty());

        assertThatThrownBy(() -> 
                availabilityService.update(
                        userId, 
                        availabilityId, 
                        request
                ))
                .isInstanceOf(AvailabilityNotFoundException.class);

        verify(availabilityRepository, never())
                .save(any());
    }

    @Test
    void shouldRejectUpdateWhenItOverlapsAnotherAvailability() {
        Availability existing = new Availability(
                userId, 
                DayOfWeek.MONDAY, 
                LocalTime.of(9, 0),
                LocalTime.of(12, 0)
        );

        UUID availabilityId = existing.getId();

        Availability other = new Availability(
                userId, 
                DayOfWeek.MONDAY, 
                LocalTime.of(14, 0),
                LocalTime.of(17, 0)
        );

        UpdateAvailabilityRequest request = 
                new UpdateAvailabilityRequest(
                        DayOfWeek.MONDAY,
                        LocalTime.of(11, 0),
                        LocalTime.of(15, 0)
                );
        
        when(availabilityRepository.findByIdAndUserId(
                availabilityId, 
                userId
        )).thenReturn(Optional.of(existing));

        when(availabilityRepository.findByUserIdAndDayOfWeek(
                userId, 
                DayOfWeek.MONDAY
        )).thenReturn(List.of(existing, other));

        assertThatThrownBy(() -> 
                availabilityService.update(
                        userId, 
                        availabilityId, 
                        request
                ))
                .isInstanceOf(InvalidAvailabilityException.class)
                .hasMessage("Availability periods must not overlap");

        verify(availabilityRepository, never())
                .save(any());
    }

    @Test 
    void shouldDeleteAvailability() {
        Availability availability = new Availability(
                userId, 
                DayOfWeek.MONDAY, 
                LocalTime.of(9, 0),
                LocalTime.of(12, 0)
        );

        UUID availabilityId = availability.getId();

        when(availabilityRepository.findByIdAndUserId(
                availabilityId, 
                userId
        )).thenReturn(Optional.of(availability));

        availabilityService.delete(userId, availabilityId);

        verify(availabilityRepository).delete(availability);
    }

    @Test 
    void shouldRejectDeleteWhenAvailabilityDoesNotBelongToUser() {
        UUID availabilityId = UUID.randomUUID();

        when(availabilityRepository.findByIdAndUserId(
                availabilityId, 
                userId
        )).thenReturn(Optional.empty());

        assertThatThrownBy(() -> 
                availabilityService.delete(userId, availabilityId)
        )
                .isInstanceOf(AvailabilityNotFoundException.class);
        
        verify(availabilityRepository, never())
                .delete(any());

    }
}