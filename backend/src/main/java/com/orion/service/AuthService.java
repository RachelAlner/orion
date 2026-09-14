package com.orion.service;

import com.orion.service.UserService;
import com.orion.model.User;
import com.orion.security.JwtService;
import com.orion.dto.AuthResponse;
import com.orion.exception.*;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service 
public class AuthService {

    private final JwtService jwtService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserService userService, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse register(String email, String password) {
        
        String normalisedEmail = email.trim().toLowerCase();

        if (userService.existsByEmail(normalisedEmail)) {
            throw new DuplicateEmailException("Email is already registered");
        }

        String passwordHash = passwordEncoder.encode(password);

        User user = new User(normalisedEmail, passwordHash);

        User savedUser = userService.save(user);
        
        String token = jwtService.generateToken(savedUser);

        return new AuthResponse(user.getId(), user.getEmail(), token);
    }

    public AuthResponse authenticate(String email, String password) {
        
        String normalisedEmail = email.trim().toLowerCase();

        User user = userService.findByEmail(normalisedEmail).orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));
    
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        String token = jwtService.generateToken(user);
        
        return new AuthResponse(user.getId(), user.getEmail(), token);
    }
}