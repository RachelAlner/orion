package com.orion.service;

import com.orion.model.User;
import com.orion.dto.AuthResponse;
import com.orion.repository.UserRepository;
import com.orion.security.JwtService;
import com.orion.exception.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    private static final String EMAIL = "test@example.com";
    private static final String PASSWORD = "password123";
    private static final String HASH = "hashed-password";
    private static final String TOKEN = "jwt-token";

    // authenticate 
    @Mock
    private UserService userService;

    @Mock 
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AuthService authService;

    @BeforeEach 
    void setUp() {
        authService = new AuthService(userService, passwordEncoder, jwtService);
    }

    @Test 
    void shouldRegisterUserWithHashedPassword() {
        User savedUser = new User(EMAIL, HASH);

        when(userService.existsByEmail(EMAIL)).thenReturn(false);

        when(passwordEncoder.encode(PASSWORD)).thenReturn(HASH);

        when(userService.save(any(User.class))).thenReturn(savedUser);

        when (jwtService.generateToken(savedUser)).thenReturn("jwt-token");

        AuthResponse response = authService.register(EMAIL, PASSWORD);

        assertEquals(EMAIL, response.email());
        assertEquals(TOKEN, response.token());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        verify(userService).save(userCaptor.capture());

        User user = userCaptor.getValue();

        assertEquals(EMAIL, user.getEmail());
        assertEquals(HASH, user.getPasswordHash());
        assertEquals(user.getId(), response.userId());

        verify(passwordEncoder).encode(PASSWORD);
        verify(jwtService).generateToken(savedUser);
    }

    @Test 
    void shouldRejectAlreadyRegisteredEmail() {
        when(userService.existsByEmail(EMAIL)).thenReturn(true);

        assertThrows(DuplicateEmailException.class, () -> authService.register(EMAIL, PASSWORD));

        verify(passwordEncoder, never()).encode(anyString());

        verify(userService, never()).save(any(User.class));

        verify(jwtService, never()).generateToken(any(User.class));
    }

    @Test 
    void shouldNormaliseEmailWhenRegistering() {
        String unnormalisedEmail = "  TEST@EXAMPLE.COM  ";

        when(userService.existsByEmail(EMAIL)).thenReturn(false);

        User savedUser = new User(EMAIL, HASH);

        when(userService.save(any(User.class))).thenReturn(savedUser);

        when(jwtService.generateToken(savedUser)).thenReturn(TOKEN);

        authService.register(unnormalisedEmail, PASSWORD);

        verify(userService).existsByEmail(EMAIL);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        verify(userService).save(userCaptor.capture());

        assertEquals(EMAIL, userCaptor.getValue().getEmail());
    }

    @Test
    void shouldAuthenticateUserWithCorrectPassword() {
        User user = new User(EMAIL, HASH);

        givenExistingUser(user);
        
        when(passwordEncoder.matches(PASSWORD, HASH)).thenReturn(true);

        when(jwtService.generateToken(user)).thenReturn(TOKEN);

        AuthResponse result = authService.login(EMAIL, PASSWORD);

        assertEquals(user.getId(), result.userId());
        assertEquals(EMAIL, result.email());
        assertEquals(TOKEN, result.token());

        verify(passwordEncoder).matches(PASSWORD, HASH);

        verify(jwtService).generateToken(user);
    }

    @Test
    void shouldRejectIncorrectPassword() {

        User user = new User(EMAIL, HASH);

        givenExistingUser(user);
        when(passwordEncoder.matches(PASSWORD, HASH)).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> authService.login(EMAIL, PASSWORD));

        verify(jwtService, never()).generateToken(any(User.class));
    }

    @Test 
    void shouldRejectNonexistentUser() {

        when(userService.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> authService.login(EMAIL, PASSWORD));

        verify(passwordEncoder, never()).matches(anyString(), anyString());

        verify(jwtService, never()).generateToken(any(User.class));
    }

    @Test 
    void shouldNormaliseEmailWhenAuthenticating() {
        User user = new User(EMAIL, HASH);

        givenExistingUser(user);

        when(passwordEncoder.matches(PASSWORD, HASH)).thenReturn(true);

        when(jwtService.generateToken(user)).thenReturn(TOKEN);

        authService.login("  TEST@EXAMPLE.COM  ", PASSWORD);

        verify(userService).findByEmail(EMAIL);
    }

    @Test
    void shouldNotGenerateTokenWhenAuthenticationFails() {
        User user = new User(EMAIL, HASH);

        givenExistingUser(user);

        when(passwordEncoder.matches(PASSWORD, HASH)).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> authService.login(EMAIL, PASSWORD));

        verify(jwtService, never()).generateToken(any(User.class));
    }

    private void givenExistingUser(User user) {
        when (userService.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    }
}