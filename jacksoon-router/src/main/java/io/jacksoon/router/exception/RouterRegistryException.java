package io.jacksoon.router.exception;

public class RouterRegistryException extends RouterException {
    public RouterRegistryException(String message) {
        super(message);
    }

    public RouterRegistryException(String message, Throwable cause) {
        super(message, cause);
    }
}
