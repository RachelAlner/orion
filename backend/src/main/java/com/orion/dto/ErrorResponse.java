package com.orion.dto;

public record ErrorResponse(
    String code, 
    String message
) {
    
}