package com.orion.exception;

public class AvailabilityNotFoundException extends RuntimeException {

    public AvailabilityNotFoundException() {
        super("Availability not found");
    }
}