package io.jacksoon.filterManagement.pipeline.util;

import io.jacksoon.common.filter.*;
import io.jacksoon.common.pipeline.context.HttpRequest;
import io.jacksoon.filterManagement.pipeline.context.FilterPipelineContext;
import io.jacksoon.init.annotation.Init;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;

@Init
public class FilterRequestParser {

    public FilterUploadRequest parseUpload(FilterPipelineContext context) {
        HttpRequest request = context.getRequest();
        byte[] fileBytes = request.getBody();
        if (fileBytes == null || fileBytes.length == 0) {
            throw new IllegalArgumentException("Filter file body is empty");
        }
        validateFileHash(fileBytes, requireHeader(request, "Filter-File-Hash"));
        FilterConfigDto config = new FilterConfigDto(
                requireHeader(request, "Filter-Name"),
                requireHeader(request, "Class-Name"),
                parseEnum(FilterTiming.class, requireHeader(request, "Filter-Timing"), "Filter-Timing"),
                parseEnum(PipelineType.class, requireHeader(request, "Filter-Pipeline"), "Filter-Pipeline"),
                parseOrder(requireHeader(request, "Filter-Order")),
                parseEnum(FilterFileType.class, requireHeader(request, "Filter-File-Type"), "Filter-File-Type")
        );
        return new FilterUploadRequest(fileBytes, config);
    }
    public String parseFilterName(FilterPipelineContext context) {
        return requireHeader(context.getRequest(), "Filter-Name");
    }

    private void validateFileHash(byte[] fileBytes, String expectedHash) {
        if (!expectedHash.matches("(?i)[0-9a-f]{64}")) {
            throw new IllegalArgumentException("Invalid Filter-File-Hash header");
        }

        String actualHash = calculateSha256(fileBytes);
        if (!actualHash.equalsIgnoreCase(expectedHash)) {
            throw new IllegalArgumentException("Filter file hash mismatch");
        }
    }

    private String calculateSha256(byte[] bytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(bytes));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 algorithm is unavailable", exception);
        }
    }

    private <E extends Enum<E>> E parseEnum(Class<E> enumType, String value, String headerName) {
        try {
            return Enum.valueOf(enumType, value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException();
        }
    }

    private int parseOrder(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException();
        }
    }

    private String requireHeader(HttpRequest request, String headerName) {
        String value = getHeader(request, headerName);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException();
        }
        return value.trim();
    }

    private String getHeader(HttpRequest request, String headerName) {
        Map<String, String> headers = request.getHeaders();
        if (headers == null) {
            return null;
        }
        return headers.entrySet().stream()
                .filter(entry -> entry.getKey().equalsIgnoreCase(headerName))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }
}
