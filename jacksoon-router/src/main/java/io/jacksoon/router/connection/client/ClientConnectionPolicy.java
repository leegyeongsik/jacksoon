package io.jacksoon.router.connection.client;

import io.jacksoon.router.exception.RouterConfigurationException;
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
            throw new RouterConfigurationException("check interval must be greater than zero");
        }
        if (idleTimeoutMillis <= 0) {
            throw new RouterConfigurationException("idle timeout must be greater than zero");
        }
        if (coldToWarmMinRequestPerSecond < 0 || warmToHotMinRequestPerSecond < 0 || warmToColdMaxRequestPerSecond < 0 || hotToWarmMaxRequestPerSecond < 0) {
            throw new RouterConfigurationException("request rate threshold must not be negative");
        }
    }

    public long checkIntervalMillis(ClientConnectionTier tier) {
        return switch (tier) {
            case COLD -> coldCheckIntervalMillis;
            case WARM -> warmCheckIntervalMillis;
            case HOT -> hotCheckIntervalMillis;
            case CLOSE -> throw new RouterConfigurationException("CLOSE tier does not use monitor interval");
        };
    }
}