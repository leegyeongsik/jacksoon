package io.jacksoon.common.util;

public record ResponseCheckResult(
        boolean complete,
        int responseLength,
        int headerLength,
        boolean closeByEof
) {
}