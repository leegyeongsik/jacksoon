package io.jacksoon.router.pipeline.context;

import io.jacksoon.router.help.BufferContext;
import io.jacksoon.router.pipeline.step.Step;
import lombok.Getter;
import lombok.Setter;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

@Getter
@Setter
public class PipelineContext {
    private SocketChannel socketChannel;
    private String event;
    private ByteBuffer byteBuffer;
    private int byteBufferIndex;
    private RouterRequest request;
    private RouterResponse response;

    public PipelineContext(SocketChannel socketChannel, String event, ByteBuffer byteBuffer, int byteBufferIndex, BufferContext bufferContext , SelectionKey selectionKey) {
        this.socketChannel = socketChannel;
        this.event = event;
        this.byteBuffer = byteBuffer;
        this.byteBufferIndex = byteBufferIndex;
        this.request = new RouterRequest();
        this.response = new RouterResponse(bufferContext,selectionKey);
    }
}
