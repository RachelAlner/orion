package com.orion.repository;

import com.orion.model.Availability;
import com.orion.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AvailabilityRepositoryTest {

    @Autowired 
    private AvailabilityRepository availabilityRepository;

    @Autowired 
    private UserRepository userRepository;

    @Test 
    void shouldSaveAndFindAvailability() {
        User user = createUser("availability-save@example.com");

        Availability availability = new Availability(
                user.getId(),
                DayOfWeek.MONDAY,
                LocalTime.of(9, 0), 
                LocalTime.of(12, 0)
        );

        Availability saved = availabilityRepository.save(availability);

        Optional<Availability> result = availabilityRepository.findById(saved.getId());

        assertThat(result).isPresent();
        assertEquals(result.get().getUserId(), user.getId());
        assertEquals(result.get().getDayOfWeek(), DayOfWeek.MONDAY);
        assertEquals(result.get().getStartTime(), LocalTime.of(9, 0));
        assertEquals(result.get().getEndTime(), LocalTime.of(12, 0));
    }

    @Test 
    void shouldFindAvailabilityForUserInDayAndStartTimeOrder() {
        User user = createUser("availability-order@example.com");

        Availability mondayAfternoon = new Availability(
                user.getId(), 
                DayOfWeek.MONDAY, 
                LocalTime.of(14, 0), 
                LocalTime.of(17, 0)
        );

        Availability mondayMorning = new Availability(
                user.getId(),
                DayOfWeek.MONDAY, 
                LocalTime.of(9, 0),
                LocalTime.of(12, 0)
        );

        Availability tuesday = new Availability(
                user.getId(), 
                DayOfWeek.TUESDAY, 
                LocalTime.of(10, 0), 
                LocalTime.of(12, 0)
        );

        availabilityRepository.save(mondayAfternoon);
        availabilityRepository.save(mondayMorning);
        availabilityRepository.save(tuesday);

        List<Availability> result = 
                availabilityRepository.findByUserIdOrderByDayOfWeekAscStartTimeAsc(
                        user.getId()
                );
        
        assertThat(result).hasSize(3);

        assertEquals(result.get(0).getDayOfWeek(), DayOfWeek.MONDAY);
        assertEquals(result.get(0).getStartTime(), LocalTime.of(9, 0));

        assertEquals(result.get(1).getDayOfWeek(), DayOfWeek.MONDAY);
        assertEquals(result.get(1).getStartTime(), LocalTime.of(14, 0));

        assertEquals(result.get(2).getDayOfWeek(), DayOfWeek.TUESDAY);
        assertEquals(result.get(2).getStartTime(), LocalTime.of(10, 0));

    }

    @Test 
    void shouldOnlyFindAvailabilityForRequestedUser() {
        User userOne = createUser("availability-user-one@example.com");
        User userTwo = createUser("availability-user-two@example.com");

        Availability userOneAvailability = new Availability(
                userOne.getId(), 
                DayOfWeek.MONDAY, 
                LocalTime.of(9, 0), 
                LocalTime.of(12, 0)
        );

        Availability userTwoAvailability = new Availability(
                userTwo.getId(), 
                DayOfWeek.MONDAY, 
                LocalTime.of(10, 0), 
                LocalTime.of(13, 0)
        );

        availabilityRepository.save(userOneAvailability);
        availabilityRepository.save(userTwoAvailability);

        List<Availability> result = 
                availabilityRepository.findByUserIdOrderByDayOfWeekAscStartTimeAsc(
                        userOne.getId()
                );
        
        assertThat(result).hasSize(1);
        assertEquals(result.get(0).getUserId(), userOne.getId());
    }

    @Test 
    void shouldFindAvailabilityForUserAndDay() {
        User user = createUser("availability-day@example.com");

        Availability mondayMorning = new Availability(
                user.getId(),
                DayOfWeek.MONDAY,
                LocalTime.of(9, 0), 
                LocalTime.of(12, 0)
        );

        Availability mondayAfternoon = new Availability(
                user.getId(), 
                DayOfWeek.MONDAY, 
                LocalTime.of(14, 0), 
                LocalTime.of(17, 0)
        );

        Availability tuesday = new Availability(
                user.getId(), 
                DayOfWeek.TUESDAY, 
                LocalTime.of(10, 0), 
                LocalTime.of(12, 0)
        );

        availabilityRepository.save(mondayMorning);
        availabilityRepository.save(mondayAfternoon);
        availabilityRepository.save(tuesday);

        List<Availability> result = 
                availabilityRepository.findByUserIdAndDayOfWeek(
                        user.getId(), 
                        DayOfWeek.MONDAY
                );
        
        assertThat(result).hasSize(2);
        assertThat(result)
                .allMatch(availability -> 
                        availability.getDayOfWeek() == DayOfWeek.MONDAY);
    }

    @Test 
    void shouldFindAvailabilityWhenIdAndUserIdMatch() {
        User user = createUser("availability-owner@example.com");

        Availability availability = new Availability(
                user.getId(), 
                DayOfWeek.WEDNESDAY, 
                LocalTime.of(9, 0), 
                LocalTime.of(12, 0)
        );

        Availability saved = availabilityRepository.save(availability);

        Optional<Availability> result = 
                availabilityRepository.findByIdAndUserId(
                        saved.getId(),
                        user.getId()
                );
        
        assertThat(result).isPresent();
        assertEquals(result.get().getId(), saved.getId());
    }

    @Test 
    void shouldNotFindAvailabilityWhenUserIdDoesNotMatch() {
        User owner = createUser("availability-owner-two@example.com");
        User otherUser = createUser("availability-other-user@example.com");

        Availability availability = new Availability(
                owner.getId(), 
                DayOfWeek.THURSDAY, 
                LocalTime.of(9, 0), 
                LocalTime.of(12, 0)
        );

        Availability saved = availabilityRepository.save(availability);

        Optional<Availability> result = 
                availabilityRepository.findByIdAndUserId(
                        saved.getId(), 
                        otherUser.getId()
                );
        
        assertThat(result).isEmpty();
    }

    private User createUser(String email) {
        User user = new User(email, "password-hash");
        return userRepository.save(user);
    }
}