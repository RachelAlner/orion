package com.orion.exception;

public class TaskDependencyNotFoundException extends RuntimeException {

    public TaskDependencyNotFoundException(String message){
        super(message);
    }
}