package io.jacksoon.registry.exception.executor;

import io.jacksoon.common.exception.ExceptionExecutor;
import io.jacksoon.init.annotation.Init;
import io.jacksoon.registry.connection.EndpointConnectionContext;
import io.jacksoon.registry.exception.RegistryConnectionException;
import io.jacksoon.registry.exception.context.RegistryExceptionContext;
import io.jacksoon.registry.store.RegistryStore;

@Init
public class RegistryConnectionExceptionExecutor implements ExceptionExecutor<RegistryExceptionContext> {
    private final RegistryStore registryStore;

    public RegistryConnectionExceptionExecutor(RegistryStore registryStore) {
        this.registryStore = registryStore;
    }

    @Override
    public Class<RegistryExceptionContext> contextType() {
        return RegistryExceptionContext.class;
    }

    @Override
    public Class<? extends Throwable> exceptionType() {
        return RegistryConnectionException.class;
    }

    @Override
    public void execute(RegistryExceptionContext context, Throwable throwable) {
        EndpointConnectionContext connectionContext = context == null
                ? null
                : context.getEndpointConnectionContext();

        if (connectionContext != null) {
            registryStore.removeEndpoint(connectionContext.getServiceName(), connectionContext.getInstanceId(), connectionContext.getRegistrationId());
        }
        throwable.printStackTrace();
    }
}
