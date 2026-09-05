package io.jacksoon.common.connection;

import lombok.Getter;

@Getter
public class ConnectionContext {
    String host;
    int port;
    public ConnectionContext(String host, int port){
        this.host = host;
        this.port = port;
    }
}
