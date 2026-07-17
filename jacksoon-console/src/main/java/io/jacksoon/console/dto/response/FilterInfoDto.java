package io.jacksoon.console.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FilterInfoDto {
    private Long filterInfoId;
    private String filterName;
    private String className;
    private int order;
    private boolean active;
    private long success;
    private long failure;
}
