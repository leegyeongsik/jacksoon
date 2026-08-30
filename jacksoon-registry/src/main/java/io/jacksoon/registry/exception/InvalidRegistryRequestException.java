package io.jacksoon.registry.exception;

public class InvalidRegistryRequestException extends RegistryException {
    public InvalidRegistryRequestException(String message) {
        super(message);
    }

    public InvalidRegistryRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
