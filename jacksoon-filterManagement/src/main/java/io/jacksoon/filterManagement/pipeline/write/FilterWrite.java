package io.jacksoon.filterManagement.pipeline.write;

import io.jacksoon.common.pipeline.context.HttpResponse;
import io.jacksoon.filterManagement.pipeline.context.FilterPipelineContext;
import io.jacksoon.filterManagement.pipeline.depth.FilterDepth;
import io.jacksoon.init.annotation.Init;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Init
public class FilterWrite implements FilterDepth {
    @Override
    public void dodo(FilterPipelineContext context) {
        HttpResponse response = context.getResponse();
        ByteBuffer body = response.getWriteBuffer();
        if (body == null) {
            body = ByteBuffer.wrap(new byte[0]);
        } else {
            body = body.duplicate();
        }

        response.addHeader("Content-Length", String.valueOf(body.remaining()));
        response.addHeader("Connection", "close");

        byte[] headerBytes = createResponseHeader(response).getBytes(StandardCharsets.UTF_8);
        ByteBuffer output = ByteBuffer.allocate(headerBytes.length + body.remaining());
        output.put(headerBytes);
        output.put(body);
        output.flip();

        context.getBufferContext().setResponseBuffer(output);
        SelectionKey key = context.getSelectionKey();
        if (key == null || !key.isValid()) {
            throw new IllegalArgumentException();
        }
        key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
        key.selector().wakeup();
        context.setEvent(null);
    }

    private String createResponseHeader(HttpResponse response) {
        StringBuilder builder = new StringBuilder();
        builder.append("HTTP/1.1 ")
                .append(response.getStatusCode())
                .append(' ')
                .append(response.getReasonPhrase())
                .append("\r\n");

        for (Map.Entry<String, String> header : response.getHeaders().entrySet()) {
            builder.append(header.getKey())
                    .append(": ")
                    .append(header.getValue())
                    .append("\r\n");
        }
        return builder.append("\r\n").toString();
    }

    @Override
    public String currentEvent() {
        return "write";
    }
}
