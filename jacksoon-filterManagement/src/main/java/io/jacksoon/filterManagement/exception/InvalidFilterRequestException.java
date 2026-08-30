package io.jacksoon.filterManagement.exception;

public class InvalidFilterRequestException extends FilterManagementException {
    public InvalidFilterRequestException(String message) { super(message); }
    public InvalidFilterRequestException(String message, Throwable cause) { super(message, cause); }
}
