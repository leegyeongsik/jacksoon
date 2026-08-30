package io.jacksoon.filterManagement.exception;

public class InvalidFilterResponseException extends FilterManagementException {
    public InvalidFilterResponseException(String message) {
        super(message);
    }

    public InvalidFilterResponseException(String message, Throwable cause) {
        super(message, cause);
    }
}
