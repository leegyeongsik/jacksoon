package io.jacksoon.registry.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EndpointInfo {
    private String host;
    private int port;
    private String protocol;
    private String healthPath;
}