package io.jacksoon.registry.pipeline.task;

import io.jacksoon.common.registry.dto.response.RegistryVersionResponse;
import io.jacksoon.init.annotation.Init;
import io.jacksoon.registry.pipeline.context.RegistryPipelineContext;
import io.jacksoon.registry.pipeline.depth.RegistryDepth;
import io.jacksoon.registry.store.RegistryStore;

@Init
public class RegistryVersionQuery implements RegistryDepth {
    private final RegistryStore registryStore;

    public RegistryVersionQuery(RegistryStore registryStore) {
        this.registryStore = registryStore;
    }

    @Override
    public void dodo(RegistryPipelineContext context) {
        context.getResponse().setStatusCode(200);
        context.getResponse().setReasonPhrase("OK");
        context.getResponse().setBody(new RegistryVersionResponse(registryStore.version()));
        context.getResponse().addHeader("Content-Type", "application/json");
        context.setEvent("write");
    }

    @Override
    public String currentEvent() {
        return "/version";
    }
}
