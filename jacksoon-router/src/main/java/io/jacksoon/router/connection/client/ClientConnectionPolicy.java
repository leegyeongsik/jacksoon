package io.jacksoon.router.connection.client;

public record ClientConnectionPolicy(
        long coldCheckIntervalMillis,
        long warmCheckIntervalMillis,
        long hotCheckIntervalMillis,
        long idleTimeoutMillis,
        double coldToWarmMinRequestPerSecond,
        double warmToHotMinRequestPerSecond,
        double warmToColdMaxRequestPerSecond,
        double hotToWarmMaxRequestPerSecond
) {
    public ClientConnectionPolicy {
        if (coldCheckIntervalMillis <= 0 || warmCheckIntervalMillis <= 0 || hotCheckIntervalMillis <= 0) {
            throw new IllegalArgumentException("check interval must be greater than zero");
        }
        if (idleTimeoutMillis <= 0) {
            throw new IllegalArgumentException("idle timeout must be greater than zero");
        }
        if (coldToWarmMinRequestPerSecond < 0 || warmToHotMinRequestPerSecond < 0 || warmToColdMaxRequestPerSecond < 0 || hotToWarmMaxRequestPerSecond < 0) {
            throw new IllegalArgumentException("request rate threshold must not be negative");
        }
    }

    public long checkIntervalMillis(ClientConnectionTier tier) {
        return switch (tier) {
            case COLD -> coldCheckIntervalMillis;
            case WARM -> warmCheckIntervalMillis;
            case HOT -> hotCheckIntervalMillis;
            case CLOSE -> throw new IllegalArgumentException("CLOSE tier does not use monitor interval");
        };
    }
}