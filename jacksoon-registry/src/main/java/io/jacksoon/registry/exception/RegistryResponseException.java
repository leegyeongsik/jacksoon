package io.jacksoon.registry.exception;

public class RegistryResponseException extends RegistryException {
    public RegistryResponseException(String message) {
        super(message);
    }

    public RegistryResponseException(String message, Throwable cause) {
        super(message, cause);
    }
}
