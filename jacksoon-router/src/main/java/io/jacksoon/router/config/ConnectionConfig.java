package io.jacksoon.router.config;

import io.jacksoon.common.connection.ConnectionHandlerRegistry;
import io.jacksoon.common.util.HttpRequestCheck;
import io.jacksoon.common.util.HttpResponseCheck;
import io.jacksoon.init.annotation.Init;
import io.jacksoon.router.handler.BackendIOHandler;

@Init
public class ConnectionConfig {
    @Init
    public ConnectionHandlerRegistry<BackendIOHandler> connectionRegistry() {
        return new ConnectionHandlerRegistry<>();
    }

    @Init
    public HttpRequestCheck httpRequestCheck() {
        return new HttpRequestCheck();
    }

    @Init
    public HttpResponseCheck httpResponseCheck() {
        return new HttpResponseCheck();
    }
}
