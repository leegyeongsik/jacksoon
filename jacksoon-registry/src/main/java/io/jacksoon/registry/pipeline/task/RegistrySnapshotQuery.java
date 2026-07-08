package io.jacksoon.registry.pipeline.task;

import io.jacksoon.init.annotation.Init;
import io.jacksoon.registry.dto.response.RegistrySnapshot;
import io.jacksoon.registry.pipeline.context.RegistryPipelineContext;
import io.jacksoon.registry.pipeline.depth.RegistryDepth;
import io.jacksoon.registry.store.RegistryStore;

@Init
public class RegistrySnapshotQuery implements RegistryDepth {
    private final RegistryStore registryStore;
    public RegistrySnapshotQuery(RegistryStore registryStore) {
        this.registryStore = registryStore;
    }
    @Override
    public void dodo(RegistryPipelineContext context) {
        RegistrySnapshot snapshot = registryStore.snapshot();
        context.getResponse().setStatusCode(200);
        context.getResponse().setReasonPhrase("OK");
        context.getResponse().setBody(snapshot);
        context.getResponse().addHeader("Content-Type", "application/json");
        context.setEvent("write");
    }
    @Override
    public String currentEvent() {
        return "/snapshot";
    }
}
