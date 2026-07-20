package io.jacksoon.console.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ServiceInstanceDto {
    private Long serviceInstanceId;
    private String instanceId;
    private String host;
    private int port;
    private String protocol;
    private String healthPath;
}
