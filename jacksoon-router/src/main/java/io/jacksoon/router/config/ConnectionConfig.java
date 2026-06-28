package io.jacksoon.router.config;

import io.jacksoon.common.connection.ConnectionRegistry;
import io.jacksoon.common.util.HttpRequestCheck;
import io.jacksoon.common.util.HttpResponseCheck;
import io.jacksoon.init.annotation.Init;

@Init
public class ConnectionConfig {
    @Init
    public ConnectionRegistry connectionRegistry() {
        return new ConnectionRegistry();
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
