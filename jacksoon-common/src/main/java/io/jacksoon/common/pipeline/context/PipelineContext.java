package io.jacksoon.common.pipeline.context;

import lombok.Getter;
import lombok.Setter;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
@Setter
public class PipelineContext {
    private SocketChannel socketChannel;
    private String event;
    private ByteBuffer byteBuffer;
    private int byteBufferIndex;
    private HttpRequest request;
    private HttpResponse response;
    private SelectionKey selectionKey;
    private AtomicInteger current;
    private boolean closeAfterWrite;

    public PipelineContext(SocketChannel socketChannel, String event, ByteBuffer byteBuffer, int byteBufferIndex , SelectionKey selectionKey , AtomicInteger current) {
        this.socketChannel = socketChannel;
        this.event = event;
        this.byteBuffer = byteBuffer;
        this.byteBufferIndex = byteBufferIndex;
        this.request = new HttpRequest();
        this.response = new HttpResponse();
        this.selectionKey = selectionKey;
        this.current = current;
    }
}
