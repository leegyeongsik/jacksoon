package io.jacksoon.common.filter;

import io.jacksoon.common.pipeline.context.HttpRequest;
import io.jacksoon.common.pipeline.context.HttpResponse;
import io.jacksoon.common.util.BufferContext;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public interface FilterContext {

    SocketChannel getSocketChannel();

    String getEvent();

    void setEvent(String event);

    ByteBuffer getByteBuffer();

    void setByteBuffer(ByteBuffer byteBuffer);

    int getByteBufferIndex();

    void setByteBufferIndex(int byteBufferIndex);

    HttpRequest getRequest();

    void setRequest(HttpRequest request);

    HttpResponse getResponse();

    void setResponse(HttpResponse response);

    BufferContext getBufferContext();

    void setBufferContext(BufferContext bufferContext);

    SelectionKey getSelectionKey();

    void setSelectionKey(SelectionKey selectionKey);
}