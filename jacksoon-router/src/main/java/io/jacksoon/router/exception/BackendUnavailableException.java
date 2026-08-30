package io.jacksoon.router.exception;

import lombok.Getter;

@Getter
public class BackendUnavailableException extends BackendException {
    private final String serviceName;

    public BackendUnavailableException(String serviceName, String message) {
        super(message);
        this.serviceName = serviceName;
    }
}
