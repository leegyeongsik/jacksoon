package io.jacksoon.registry.config;

public record RegistryProperties(
        Server server,
        Console console
) {
    public record Server(int port) {}

    public record Console(String baseUrl) {}
}
