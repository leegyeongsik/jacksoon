package io.jacksoon.router.config;

public record RouterProperties(Server server, Registry registry, Filter filter, Metric metric) {
    public record Server(int port, int backlog) {}

    public record Registry(String baseUrl) {}

    public record Filter(String baseUrl, String bundleDirectory) {}

    public record Metric(String directory) {}
}
