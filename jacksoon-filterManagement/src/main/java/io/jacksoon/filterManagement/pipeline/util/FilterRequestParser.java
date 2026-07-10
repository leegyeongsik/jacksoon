package io.jacksoon.filterManagement.pipeline.util;

import io.jacksoon.common.pipeline.context.HttpRequest;
import io.jacksoon.filterManagement.pipeline.context.FilterConfigDto;
import io.jacksoon.filterManagement.pipeline.context.FilterFileType;
import io.jacksoon.filterManagement.pipeline.context.FilterPipelineContext;
import io.jacksoon.filterManagement.pipeline.context.FilterTiming;
import io.jacksoon.filterManagement.pipeline.context.FilterUploadRequest;
import io.jacksoon.filterManagement.pipeline.context.PipelineType;
import io.jacksoon.init.annotation.Init;

import java.util.Map;

@Init
public class FilterRequestParser {

    public FilterUploadRequest parse(FilterPipelineContext filterPipelineContext) {
        HttpRequest request = filterPipelineContext.getRequest();

        byte[] fileBytes = request.getBody();
        if (fileBytes == null || fileBytes.length == 0) {
            throw new IllegalArgumentException("Filter file body is empty");
        }

        String filterName = requireHeader(request, "Filter-Name");

        FilterTiming timing = parseTiming(requireHeader(request, "Filter-Timing"));

        PipelineType pipeline = parsePipeline(requireHeader(request, "Filter-Pipeline")
        );

        String path = normalizePath(getHeader(request, "Filter-Path"));

        int order = parseOrder(requireHeader(request, "Filter-Order"));

        FilterFileType filterFileType = parseFilterFileType(requireHeader(request, "Filter-File-Type"));

        FilterConfigDto config = new FilterConfigDto(
                filterName,
                timing,
                pipeline,
                path,
                order,
                filterFileType
        );

        return new FilterUploadRequest(fileBytes, config,false);
    }

    private FilterTiming parseTiming(String value) {
        try {
            return FilterTiming.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid Filter-Timing: " + value, e);
        }
    }

    private PipelineType parsePipeline(String value) {
        try {
            return PipelineType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid Filter-Pipeline: " + value, e);
        }
    }

    private FilterFileType parseFilterFileType(String value) {
        try {
            return FilterFileType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid Filter-File-Type: " + value, e);
        }
    }

    private int parseOrder(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid Filter-Order: " + value, e);
        }
    }

    private String requireHeader(HttpRequest request, String headerName) {
        String value = getHeader(request, headerName);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing required header: " + headerName);
        }

        return value.trim();
    }

    private String getHeader(HttpRequest request, String headerName) {
        Map<String, String> headers = request.getHeaders();
        if (headers == null) {
            return null;
        }

        return headers.entrySet()
                .stream()
                .filter(entry ->
                        entry.getKey()
                                .equalsIgnoreCase(headerName)
                )
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }

    private String normalizePath(String path) {
        if (path == null || path.isBlank()) {
            return null;
        }

        return path.trim();
    }
}