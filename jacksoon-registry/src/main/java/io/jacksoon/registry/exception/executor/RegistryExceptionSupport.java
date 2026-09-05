package io.jacksoon.registry.exception.executor;

import io.jacksoon.common.handler.IOHandler;
import io.jacksoon.common.handler.IOStore;
import io.jacksoon.common.util.ResponseContext;
import io.jacksoon.registry.exception.context.RegistryExceptionContext;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

final class RegistryExceptionSupport {
    private RegistryExceptionSupport() {
    }

    static void respond(IOStore ioStore, RegistryExceptionContext context, int statusCode, String reason) {
        if (context == null) {
            return;
        }
        SelectionKey key = context.getRequestSelectionKey();
        AtomicInteger current = context.getCurrent();
        if (key == null || current == null || !key.isValid()) {
            closeSelectionKey(key);
            return;
        }

        byte[] bodyBytes = reason.getBytes(StandardCharsets.UTF_8);
        byte[] headerBytes = (
                "HTTP/1.1 " + statusCode + " " + reason + "\r\n" +
                        "Content-Type: text/plain; charset=UTF-8\r\n" +
                        "Content-Length: " + bodyBytes.length + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes(StandardCharsets.ISO_8859_1);

        ByteBuffer response = ByteBuffer.allocate(headerBytes.length + bodyBytes.length);
        response.put(headerBytes);
        response.put(bodyBytes);
        response.flip();

        ioStore.offer(key, new ResponseContext(current.get(), response, true));
    }

    static void closeSelectionKey(SelectionKey key) {
        if (key == null) {
            return;
        }
        Object attachment = key.attachment();
        if (attachment instanceof IOHandler ioHandler) {
            ioHandler.close();
            return;
        }
        try {
            key.cancel();
        } catch (RuntimeException ignored) {
        }
        try {
            key.channel().close();
        } catch (IOException ignored) {
        }
    }
}
