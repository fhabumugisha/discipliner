package com.buseni.discipline.common.exception;

public class InvalidOperationException extends BusinessException {
    
    public InvalidOperationException(String messageKey) {
        super(messageKey);
    }

    public InvalidOperationException(String messageKey, Object... args) {
        super(messageKey, args);
    }
} 