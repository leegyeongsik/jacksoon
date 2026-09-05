package io.jacksoon.filterManagement.exception;

public class FilterSystemException extends FilterManagementException {
    public FilterSystemException(String message) {
        super(message);
    }

    public FilterSystemException(String message, Throwable cause) {
        super(message, cause);
    }
}
