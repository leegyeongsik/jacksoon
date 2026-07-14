package io.jacksoon.router.produce.dto;

public enum RouterFailureStage {
    REQUEST_READ,
    REQUEST_PARSE,
    FILTER,
    ROUTE_RESOLVE,
    BACKEND_SELECT,
    BACKEND_CONNECT,
    BACKEND_WRITE,
    BACKEND_READ,
    CLIENT_WRITE
}