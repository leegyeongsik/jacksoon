package io.jacksoon.filterManagement.exception.executor;

import io.jacksoon.common.handler.IOStore;
import io.jacksoon.common.util.ResponseContext;
import io.jacksoon.filterManagement.pipeline.context.FilterPipelineContext;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

final class FilterExceptionSupport {
    private FilterExceptionSupport() {
    }

    static void respond(IOStore ioStore, FilterPipelineContext context, int statusCode, String reason) {
        if (ioStore == null || context == null) {
            return;
        }
        SelectionKey key = context.getSelectionKey();
        AtomicInteger current = context.getCurrent();
        if (key == null || current == null || !key.isValid()) {
            return;
        }
        byte[] body = reason.getBytes(StandardCharsets.UTF_8);
        String header = "HTTP/1.1 " + statusCode + " " + reason + "\r\n"
                + "Content-Type: text/plain; charset=UTF-8\r\n"
                + "Content-Length: " + body.length + "\r\n"
                + "Connection: close\r\n"
                + "\r\n";

        byte[] headerBytes = header.getBytes(StandardCharsets.UTF_8);
        ByteBuffer output = ByteBuffer.allocate(headerBytes.length + body.length);
        output.put(headerBytes);
        output.put(body);
        output.flip();

        context.setCloseAfterWrite(true);
        context.setEvent(null);
        ioStore.offer(key, new ResponseContext(current.get(), output, true));
    }

    static void closeSelectionKey(SelectionKey key) {
        if (key == null) {
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
