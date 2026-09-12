package com.orion.service;

import com.orion.service.UserService;
import com.orion.model.User;
import com.orion.exception.*;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service 
public class AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(String email, String password) {
        
        String normalisedEmail = email.trim().toLowerCase();

        if (userService.existsByEmail(normalisedEmail)) {
            throw new DuplicateEmailException("Email is already registered");
        }

        String passwordHash = passwordEncoder.encode(password);

        User user = new User(normalisedEmail, passwordHash);

        return userService.save(user);
    }

    public User authenticate(String email, String password) {
        
        String normalisedEmail = email.trim().toLowerCase();

        User user = userService.findByEmail(normalisedEmail).orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));
    
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        return user;
    }
}