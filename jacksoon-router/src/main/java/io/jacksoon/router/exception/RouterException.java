package io.jacksoon.router.exception;

public abstract class RouterException extends RuntimeException {
    protected RouterException(String message) {
        super(message);
    }

    protected RouterException(String message, Throwable cause) {
        super(message, cause);
    }
}