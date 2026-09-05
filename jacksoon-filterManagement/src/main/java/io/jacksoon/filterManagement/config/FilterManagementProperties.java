package io.jacksoon.filterManagement.config;

public record FilterManagementProperties(Server server, Console console, Directory directory) {
    public record Server(int port) {}

    public record Console(String baseUrl) {}

    public record Directory(String work, String bundle) {}
}
