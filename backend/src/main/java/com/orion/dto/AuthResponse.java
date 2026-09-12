package com.orion.dto;

import java.util.UUID;

public record AuthResponse(UUID userId, String email, String token) {

}