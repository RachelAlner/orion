package com.orion.repository;

import com.orion.model.Schedule;
import com.orion.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ScheduleRepositoryTest {

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private UserRepository userRepository;

    @Test 
    void saveAndFindsSchedule() {
        User user = new User(
                "schedule-test@example.com",
                "hashed-password"
        );

        user = userRepository.save(user);

        LocalDateTime periodEnd = 
                LocalDateTime.of(2026, 9, 21, 0, 0);
        
        LocalDateTime periodStart = 
                LocalDateTime.of(2026, 9, 28, 0, 0);
        
        LocalDateTime generatedAt = 
                LocalDateTime.of(2026, 9, 21, 9, 0);
        
        Schedule schedule = new Schedule(
                user, 
                periodStart, 
                periodEnd, 
                generatedAt
        );

        Schedule savedSchedule = 
                scheduleRepository.save(schedule);
        

        assertNotNull(savedSchedule.getId());

        Schedule foundSchedule = 
                scheduleRepository
                        .findById(savedSchedule.getId())
                        .orElseThrow();
        
        assertEquals(foundSchedule.getUser().getId(), user.getId());

        assertEquals(foundSchedule.getPeriodStart(), periodStart);
        assertEquals(foundSchedule.getPeriodEnd(), periodEnd);
        assertEquals(foundSchedule.getGeneratedAt(), generatedAt);
        
    }

    @Test 
    void findsSchedulesForUserOrderedByGeneratedAtDescending() {
        User user = new User(
                "schedule-order-test@example.com",
                "hashed-password"
        );

        user = userRepository.save(user);

        Schedule olderSchedule = new Schedule(
                user, 
                LocalDateTime.of(2026, 9, 21, 0, 0),
                LocalDateTime.of(2026, 9, 28, 0, 0),
                LocalDateTime.of(2026, 9, 21, 9, 0)
        );

        Schedule newerSchedule = new Schedule(
                user, 
                LocalDateTime.of(2026, 9, 21, 0, 0),
                LocalDateTime.of(2026, 9, 28, 0, 0),
                LocalDateTime.of(2026, 9, 21, 12, 0)
        );

        scheduleRepository.save(olderSchedule);
        scheduleRepository.save(newerSchedule);

        List<Schedule> schedules = 
                scheduleRepository
                        .findAllByUserIdOrderByGeneratedAtDesc(user.getId());
        
        assertEquals(schedules.size(), 2);
        assertEquals(schedules.get(0).getId(), newerSchedule.getId());
        assertEquals(schedules.get(1).getId(), olderSchedule.getId());
    }

    @Test 
    void onlyFindsSchedulesBelongingToSpecifiedUser() {
        User firstUser = new User(
                "first-user@example.com",
                "hashed-password"
        );

        User secondUser = new User(
                "second-user@example.com",
                "hashed-password"
        );

        firstUser = userRepository.save(firstUser);
        secondUser = userRepository.save(secondUser);

        Schedule firstUserSchedule = new Schedule(
                firstUser, 
                LocalDateTime.of(2026, 9, 21, 0, 0),
                LocalDateTime.of(2026, 9, 28, 0, 0),
                LocalDateTime.of(2026, 9, 21, 9, 0)
        );

        Schedule secondUserSchedule = new Schedule(
            secondUser, 
            LocalDateTime.of(2026, 9, 21, 0, 0),
            LocalDateTime.of(2026, 9, 28, 0, 0),
            LocalDateTime.of(2026, 9, 21, 10, 0)
        );

        scheduleRepository.save(firstUserSchedule);
        scheduleRepository.save(secondUserSchedule);

        List<Schedule> schedules = 
                scheduleRepository
                        .findAllByUserIdOrderByGeneratedAtDesc(firstUser.getId());
                
        assertEquals(schedules.size(), 1);

        assertEquals(schedules.get(0).getId(), firstUserSchedule.getId());
        assertEquals(schedules.get(0).getUser(), firstUser);
    }
}