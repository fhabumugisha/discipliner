package com.buseni.discipline.common.exception;

/**
 * Base exception class for business logic exceptions
 */
public class BusinessException extends RuntimeException {
    private final String messageKey;
    private final Object[] args;

    public BusinessException(String messageKey) {
        this(messageKey, new Object[]{});
    }

    public BusinessException(String messageKey, Object... args) {
        super(messageKey);
        this.messageKey = messageKey;
        this.args = args;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public Object[] getArgs() {
        return args;
    }
} 