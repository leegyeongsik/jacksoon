package io.jacksoon.common.filter;

public record FilterUploadRequest(
        byte[] fileBytes,
        FilterConfigDto config
) {
}