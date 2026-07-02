package io.jacksoon.common.util;

public record RequestCheckResult(
        boolean complete,
        int requestLength,
        int headerLength
) {
}