package io.jacksoon.common.registry.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RegistryVersionResponse {
    private long version;
    public RegistryVersionResponse(long version) {
        this.version = version;
    }
}
