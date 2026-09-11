package com.orion.repository;

import com.orion.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSaveAndFindUserByEmail() {
        // arrange 
        User user = new User(
            "test@example.com",
            "hashed-password"
        );

        // act
        userRepository.save(user);

        Optional<User> result = userRepository.findByEmail("test@example.com");

        // assert 
        assertTrue(result.isPresent());
        assertEquals("test@example.com", result.get().getEmail());
        assertEquals("hashed-password", result.get().getPasswordHash());


    }
}