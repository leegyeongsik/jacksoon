package io.jacksoon.router.exception;

public class BackendConnectionException extends BackendException {
    public BackendConnectionException(String message) {
        super(message);
    }

    public BackendConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
