package io.jacksoon.common.util;

public record ResponseCheckResult(
        boolean complete,
        int responseLength,
        int headerLength,
        boolean closeDelimited,
        boolean connectionClose
) {
    public static ResponseCheckResult incomplete() {
        return new ResponseCheckResult(false, 0, 0, false, false);
    }
}