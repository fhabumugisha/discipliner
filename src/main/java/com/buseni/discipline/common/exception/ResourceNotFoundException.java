package com.buseni.discipline.common.exception;

public class ResourceNotFoundException extends BusinessException {
    
    public ResourceNotFoundException(String messageKey) {
        super(messageKey);
    }

    public ResourceNotFoundException(String messageKey, Object... args) {
        super(messageKey, args);
    }
} 