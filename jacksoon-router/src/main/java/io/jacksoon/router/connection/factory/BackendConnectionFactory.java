package io.jacksoon.router.connection.factory;

import io.jacksoon.router.connection.BackendConnectionPool;
import io.jacksoon.router.handler.BackendIOHandler;

public interface BackendConnectionFactory {
    BackendIOHandler create(BackendConnectionPool pool);
}