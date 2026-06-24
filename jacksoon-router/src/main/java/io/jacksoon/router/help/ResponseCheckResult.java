package io.jacksoon.router.help;

public record ResponseCheckResult(
        boolean complete,
        int responseLength,
        int headerLength,
        boolean closeByEof
) {
}