package io.jacksoon.router.pipeline;

import io.jacksoon.router.pipeline.step.Step;

import java.nio.channels.SocketChannel;

public class PipelineContext {
    public SocketChannel socketChannel;
    public Step step;
    public String event;
    private RouterRequest request;
    public RouterResponse response;
    public PipelineContext (SocketChannel socketChannel,Step step,String event){
        this.socketChannel = socketChannel;
        this.step = step;
        this.event = event;
    }
}
