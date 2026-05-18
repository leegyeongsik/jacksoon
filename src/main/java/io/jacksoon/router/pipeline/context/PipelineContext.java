package io.jacksoon.router.pipeline.context;

import io.jacksoon.router.pipeline.step.Step;
import lombok.Getter;
import lombok.Setter;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
@Getter
@Setter
public class PipelineContext {
    private SocketChannel socketChannel;
    private Step step;
    private String event;
    private ByteBuffer byteBuffer;
    private int byteBufferIndex;
    private RouterRequest request;
    private RouterResponse response;
    public PipelineContext (SocketChannel socketChannel,Step step,String event,ByteBuffer byteBuffer,int byteBufferIndex){
        this.socketChannel = socketChannel;
        this.step = step;
        this.event = event;
        this.byteBuffer = byteBuffer;
        this.byteBufferIndex =byteBufferIndex;
        this.request = new RouterRequest();
    }
}
