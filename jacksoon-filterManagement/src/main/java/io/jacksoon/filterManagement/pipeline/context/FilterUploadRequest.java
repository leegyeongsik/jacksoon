package io.jacksoon.filterManagement.pipeline.context;

public record FilterUploadRequest(
        byte[] fileBytes,
        FilterConfigDto config,
        boolean isUpdateVersion
) {
}