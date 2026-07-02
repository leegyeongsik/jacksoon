package io.jacksoon.registry.pipeline.task;

import io.jacksoon.init.annotation.Init;
import io.jacksoon.registry.dto.request.RegistryRegisterRequest;
import io.jacksoon.registry.pipeline.context.RegistryPipelineContext;
import io.jacksoon.registry.pipeline.depth.RegistryDepth;
import io.jacksoon.registry.pipeline.util.RegistryRegisterRequestParser;
import io.jacksoon.registry.store.RegistryStore;

@Init
public class RegistryRegister implements RegistryDepth {
    private final RegistryStore registryStore;
    private final RegistryRegisterRequestParser parser;

    public RegistryRegister(RegistryStore registryStore, RegistryRegisterRequestParser parser) {
        this.registryStore = registryStore;
        this.parser = parser;
    }
    @Override
    public void dodo(RegistryPipelineContext context) {
        RegistryRegisterRequest request = parser.parse(context.getRequest().getBody());
        registryStore.add(request);
        context.setRegisterRequest(request);
        context.setEvent("connection");
    }

    @Override
    public String currentEvent() {
        return "/register";
    }
}