package io.jacksoon.router.filter;

import io.jacksoon.common.filter.FilterContext;
import io.jacksoon.common.pipeline.context.HttpRequest;
import io.jacksoon.common.pipeline.context.HttpResponse;
import io.jacksoon.router.pipeline.context.RouterPipelineContext;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public final class RouterFilterContext implements FilterContext {

    private final RouterPipelineContext delegate;

    public RouterFilterContext(RouterPipelineContext delegate) {
        this.delegate = delegate;
    }

    @Override
    public SocketChannel getSocketChannel() {
        return delegate.getSocketChannel();
    }

    @Override
    public String getEvent() {
        return delegate.getEvent();
    }

    @Override
    public void setEvent(String event) {
        delegate.setEvent(event);
    }

    @Override
    public ByteBuffer getByteBuffer() {
        return delegate.getByteBuffer();
    }

    @Override
    public void setByteBuffer(ByteBuffer byteBuffer) {
        delegate.setByteBuffer(byteBuffer);
    }

    @Override
    public int getByteBufferIndex() {
        return delegate.getByteBufferIndex();
    }

    @Override
    public void setByteBufferIndex(int byteBufferIndex) {
        delegate.setByteBufferIndex(byteBufferIndex);
    }

    @Override
    public HttpRequest getRequest() {
        return delegate.getRequest();
    }

    @Override
    public void setRequest(HttpRequest request) {
        delegate.setRequest(request);
    }

    @Override
    public HttpResponse getResponse() {
        return delegate.getResponse();
    }

    @Override
    public void setResponse(HttpResponse response) {
        delegate.setResponse(response);
    }

    @Override
    public SelectionKey getSelectionKey() {
        return delegate.getSelectionKey();
    }

    @Override
    public void setSelectionKey(SelectionKey selectionKey) {
        delegate.setSelectionKey(selectionKey);
    }
}