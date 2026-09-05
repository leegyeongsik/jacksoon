package io.jacksoon.router.exception;

public abstract class RouterRequestException extends RouterException {
    protected RouterRequestException(String message) {
        super(message);
    }

    protected RouterRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}