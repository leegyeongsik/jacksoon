package io.jacksoon.registry.exception;

public class RegistryEventException extends RegistryException {
    public RegistryEventException(String message) {
        super(message);
    }

    public RegistryEventException(String message, Throwable cause) {
        super(message, cause);
    }
}
