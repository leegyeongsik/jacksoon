package io.jacksoon.common.filter;

public record FilterConfigDto(
        String filterName,
        String className,
        FilterTiming timing,
        PipelineType pipeline,
        String path,
        int order,
        FilterFileType filterFileType
) {
    public FilterConfigDto {
        if (filterName == null || filterName.isBlank()) {
            throw new IllegalArgumentException();
        }
        if (!filterName.matches("[a-zA-Z0-9._-]+")) {
            throw new IllegalArgumentException();
        }
        if (className == null || className.isBlank()) {
            throw new IllegalArgumentException();
        }
        if (!className.matches("[a-zA-Z_$][a-zA-Z0-9_$]*(\\.[a-zA-Z_$][a-zA-Z0-9_$]*)*")) {
            throw new IllegalArgumentException();
        }
        if (timing == null) {
            throw new IllegalArgumentException();
        }
        if (pipeline == null) {
            throw new IllegalArgumentException();
        }
        if (filterFileType == null || filterFileType == FilterFileType.UNKNOWN) {
            throw new IllegalArgumentException();
        }
        path = normalizePath(path);
    }
    private static String normalizePath(String path) {
        if (path == null || path.isBlank()) {
            return null;
        }
        String trimmed = path.trim();
        return trimmed.startsWith("/") ? trimmed : "/" + trimmed;
    }
}
