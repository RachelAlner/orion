package com.orion.exception;

import com.orion.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateEmail(DuplicateEmailException exception) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(
                    "DUPLICATE_EMAIL",
                    exception.getMessage()
                ));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException exception) {
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(new ErrorResponse(
                "INVALID_CREDENTIALS",
                exception.getMessage()
            ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
        return ResponseEntity
            .badRequest()
            .body(new ErrorResponse(
                "VALIDATION_ERROR",
                "The request contains invalid data."
            ));
    }

    @ExceptionHandler(ProjectNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleProjectNotFound(ProjectNotFoundException exception) {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse(
                "PROJECT_NOT_FOUND", 
                exception.getMessage()
            ));
    }

    @ExceptionHandler(TaskNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTaskNotFound(TaskNotFoundException exception) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(
                        "TASK_NOT_FOUND", 
                        exception.getMessage()
                ));
    }

    @ExceptionHandler(TaskDependencyNotFoundException.class) 
    public ResponseEntity<ErrorResponse> handleTaskDependencyNotFound(
            TaskDependencyNotFoundException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(
                        "TASK_DEPENDENCY_NOT_FOUND", 
                        exception.getMessage()
                ));
    }

    @ExceptionHandler(TaskDependencyAlreadyExistsException.class) 
    public ResponseEntity<ErrorResponse> handleTaskDependencyAlreadyExists(
            TaskDependencyAlreadyExistsException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(
                        "TASK_DEPENDENCY_ALREADY_EXISTS", 
                        exception.getMessage()
                ));
    }

    @ExceptionHandler(DependencyCycleException.class) 
    public ResponseEntity<ErrorResponse> handleDependencyCycle(
            DependencyCycleException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(
                        "DEPENDENCY_CYCLE", 
                        exception.getMessage()
                ));
    }

    @ExceptionHandler(InvalidTaskDependencyException.class) 
    public ResponseEntity<ErrorResponse> handleInvalidTaskDependency(
            InvalidTaskDependencyException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.valueOf(422))
                .body(new ErrorResponse(
                        "INVALID_TASK_DEPENDENCY",
                        exception.getMessage()
                ));
    }

    @ExceptionHandler(AvailabilityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAvailabilityNotFound(
                AvailabilityNotFoundException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(
                        "AVAILABILITY_NOT_FOUND", 
                        exception.getMessage()
                ));
    }

    @ExceptionHandler(InvalidAvailabilityException.class)
    public ResponseEntity<ErrorResponse> handleInvalidAvailability(
                InvalidAvailabilityException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(
                        "INVALID_AVAILABILITY", 
                        exception.getMessage()
                ));
    }
}