package com.orion.controller;

import com.orion.dto.AvailabilityResponse;
import com.orion.dto.CreateAvailabilityRequest;
import com.orion.dto.UpdateAvailabilityRequest;
import com.orion.service.AvailabilityService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/availability")
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    public AvailabilityController(AvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    @GetMapping
    public ResponseEntity<List<AvailabilityResponse>> findAll(
        Authentication authentication
    ) {
        UUID userId = getUserId(authentication);

        return ResponseEntity.ok(
                availabilityService.findAll(userId)
        );
    }

    @PostMapping
    public ResponseEntity<AvailabilityResponse> create(
            @Valid @RequestBody CreateAvailabilityRequest request, 
            Authentication authentication
    ) {
        UUID userId = getUserId(authentication);

        AvailabilityResponse response = 
                availabilityService.create(userId, request);

        URI location = URI.create(
                "/api/availability/" + response.id()
        );

        return ResponseEntity
                .created(location)
                .body(response);
    }

    @PutMapping("/{availabilityId}")
    public ResponseEntity<AvailabilityResponse> update(
            @PathVariable UUID availabilityId,
            @Valid @RequestBody UpdateAvailabilityRequest request,
            Authentication authentication
    ) {
        UUID userId = getUserId(authentication);

        AvailabilityResponse response = 
                availabilityService.update(
                        userId, 
                        availabilityId, 
                        request
                );
        
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{availabilityId}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID availabilityId, 
            Authentication authentication
    ) {
        UUID userId = getUserId(authentication);

        availabilityService.delete(
                userId, 
                availabilityId
        );

        return ResponseEntity.noContent().build();
    }

    private UUID getUserId(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }
}