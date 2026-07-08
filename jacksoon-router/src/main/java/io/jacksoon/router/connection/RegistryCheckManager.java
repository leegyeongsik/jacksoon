package io.jacksoon.router.connection;

import io.jacksoon.common.registry.dto.response.RegistrySnapshot;
import io.jacksoon.init.annotation.Init;
import io.jacksoon.router.connection.store.RegistryClient;
import io.jacksoon.router.connection.store.RouterRegistryStore;

@Init
public class RegistryCheckManager {
    private final RegistryClient registryClient;
    private final RouterRegistryStore routerRegistryStore;
    private final BackendConnectionPoolManager poolManager;

    public RegistryCheckManager(RegistryClient registryClient, RouterRegistryStore routerRegistryStore, BackendConnectionPoolManager poolManager) {
        this.registryClient = registryClient;
        this.routerRegistryStore = routerRegistryStore;
        this.poolManager = poolManager;
    }

    public void refresh() {
        RegistrySnapshot snapshot = registryClient.snapshot();
        poolManager.sync(snapshot);
        routerRegistryStore.save(snapshot);
    }

    public void refreshSafely() {
        try {
            refresh();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}