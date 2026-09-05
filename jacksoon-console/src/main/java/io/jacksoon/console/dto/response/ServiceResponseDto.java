package io.jacksoon.console.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ServiceResponseDto {
    long serviceId;
    String serviceName;
    long success;
    long failure;
    List<ServiceInstanceDto> serviceInstanceDto;
    List<ServiceRuleDto> ruleDtos;
}
