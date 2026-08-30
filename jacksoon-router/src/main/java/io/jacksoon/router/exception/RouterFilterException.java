package io.jacksoon.router.exception;

public class RouterFilterException extends RouterException {
    public RouterFilterException(String message) {
        super(message);
    }

    public RouterFilterException(String message, Throwable cause) {
        super(message, cause);
    }
}
