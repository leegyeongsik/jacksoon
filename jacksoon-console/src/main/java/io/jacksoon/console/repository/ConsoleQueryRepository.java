package io.jacksoon.console.repository;

import io.jacksoon.console.dto.response.FilterResponseDto;
import io.jacksoon.console.dto.response.ServiceResponseDto;

import java.util.List;

public interface ConsoleQueryRepository {
    List<ServiceResponseDto> getServices();
    List<FilterResponseDto> getFilters();


}