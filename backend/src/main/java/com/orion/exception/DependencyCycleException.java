package com.orion.exception;

public class DependencyCycleException extends RuntimeException {

    public DependencyCycleException(String message) {
        super(message);
    }
}