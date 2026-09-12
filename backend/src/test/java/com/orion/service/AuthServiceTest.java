package com.orion.service;

import com.orion.model.User;
import com.orion.exception.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AuthService authService;

    @BeforeEach 
    void setUp() {
        authService = new AuthService(userService, passwordEncoder);
    }

    @Test 
    void shouldRegisterUserWithHashedPassword() {

        String email = "test@example.com";
        String password = "password123";
        String hash = "$2a$10$hashed-password";

        when(userService.existsByEmail("test@example.com")).thenReturn(false);

        when(passwordEncoder.encode(password)).thenReturn(hash);

        User savedUser = new User(email, hash);

        when(userService.save(any(User.class))).thenReturn(savedUser);

        User result = authService.register(email, password);

        assertEquals(email, result.getEmail());
        assertEquals(hash, result.getPasswordHash());

        verify(passwordEncoder).encode(password);
        verify(userService).save(any(User.class));
    }

    @Test 
    void shouldRejectAlreadyRegisteredEmail() {
        when(userService.existsByEmail("test@example.com")).thenReturn(true);

        assertThrows(DuplicateEmailException.class, () -> authService.register("test@example.com", "password123"));

        verify(passwordEncoder, never()).encode(anyString());

        verify(userService, never()).save(any(User.class));
    }

    @Test
    void shouldAuthenticateUserWithCorrectPassword() {
        User user = new User("test@example.com", "hashed-password");

        when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        when(passwordEncoder.matches("password123", "hashed-password")).thenReturn(true);

        User result = authService.authenticate("test@example.com", "password123");

        assertEquals(user, result);

        verify(passwordEncoder).matches("password123", "hashed-password");
    }

    @Test
    void shouldRejectIncorrectPassword() {

        User user = new User("test@example.com", "hashed-password");

        when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        when(passwordEncoder.matches("wrong-password", "hashed-password")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> authService.authenticate("test@example.com", "wrong-password"));
    }

    @Test 
    void shouldRejectNonexistentUser() {

        when(userService.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> authService.authenticate("missing@example.com", "password123"));

        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }
}