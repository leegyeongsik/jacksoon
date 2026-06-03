package io.jacksoon.router.init.registration;

import lombok.Getter;
import lombok.Setter;

@Getter
public class InitInstance {
    @Setter
    Object object;
    final InitMetadata initMetadata;
    public InitInstance(InitMetadata initMetadata) {
        this.initMetadata = initMetadata;
    }
}
