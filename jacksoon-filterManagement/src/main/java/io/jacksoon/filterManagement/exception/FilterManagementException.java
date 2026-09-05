package io.jacksoon.filterManagement.exception;

public abstract class FilterManagementException extends RuntimeException {
    protected FilterManagementException(String message) {
        super(message);
    }

    protected FilterManagementException(String message, Throwable cause) {
        super(message, cause);
    }
}
