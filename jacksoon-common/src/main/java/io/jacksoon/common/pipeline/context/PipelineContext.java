package io.jacksoon.common.pipeline.context;

import io.jacksoon.common.util.BufferContext;
import lombok.Getter;
import lombok.Setter;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

@Getter
@Setter
public class PipelineContext {
    private SocketChannel socketChannel;
    private String event;
    private ByteBuffer byteBuffer;
    private int byteBufferIndex;
    private HttpRequest request;
    private HttpResponse response;
    private BufferContext bufferContext;
    private SelectionKey selectionKey;

    public PipelineContext(SocketChannel socketChannel, String event, ByteBuffer byteBuffer, int byteBufferIndex, BufferContext bufferContext , SelectionKey selectionKey) {
        this.socketChannel = socketChannel;
        this.event = event;
        this.byteBuffer = byteBuffer;
        this.byteBufferIndex = byteBufferIndex;
        this.request = new HttpRequest();
        this.response = new HttpResponse();
        this.bufferContext = bufferContext;
        this.selectionKey = selectionKey;
    }
}
