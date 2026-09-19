package com.orion.exception;

public class TaskDependencyAlreadyExistsException extends RuntimeException {

    public TaskDependencyAlreadyExistsException(String message) {
        super(message);
    }
}