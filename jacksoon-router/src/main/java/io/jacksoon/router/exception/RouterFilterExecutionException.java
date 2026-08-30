package io.jacksoon.router.exception;

public class RouterFilterExecutionException extends RouterException {
    public RouterFilterExecutionException(String message) {
        super(message);
    }

    public RouterFilterExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
