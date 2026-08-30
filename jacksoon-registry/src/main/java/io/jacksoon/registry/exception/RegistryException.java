package io.jacksoon.registry.exception;

public abstract class RegistryException extends RuntimeException {
    protected RegistryException(String message) {
        super(message);
    }

    protected RegistryException(String message, Throwable cause) {
        super(message, cause);
    }
}
