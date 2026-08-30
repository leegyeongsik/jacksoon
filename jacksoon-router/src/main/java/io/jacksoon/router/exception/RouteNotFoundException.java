package io.jacksoon.router.exception;

import lombok.Getter;

@Getter
public class RouteNotFoundException extends RouterRequestException {
    private final String path;

    public RouteNotFoundException(String path) {
        super("No route matched. path=" + path);
        this.path = path;
    }

}
