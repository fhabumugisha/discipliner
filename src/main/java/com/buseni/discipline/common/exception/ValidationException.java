package com.buseni.discipline.common.exception;

/**
 * Exception thrown when validation fails for a request
 */
public class ValidationException extends RuntimeException {
    
    public ValidationException(String message) {
        super(message);
    }
    
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
} 