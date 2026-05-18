package io.jacksoon.router.help;

public record RequestCheckResult(
        boolean complete,
        int requestLength,
        int headerLength
) {
}