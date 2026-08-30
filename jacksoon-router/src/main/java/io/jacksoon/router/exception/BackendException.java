package io.jacksoon.router.exception;

public abstract class BackendException extends RouterException {
    protected BackendException(String message) {
        super(message);
    }

    protected BackendException(String message, Throwable cause) {
        super(message, cause);
    }
}