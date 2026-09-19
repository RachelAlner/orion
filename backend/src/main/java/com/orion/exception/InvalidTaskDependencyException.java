package com.orion.exception;

public class InvalidTaskDependencyException extends RuntimeException {

    public InvalidTaskDependencyException(String message) {
        super(message);
    }
}