package com.infratrack.exception;

public class ForbiddenOperationException extends BusinessException {

    public ForbiddenOperationException(String message) {
        super(message);
    }
}
