package io.jacksoon.router.pipeline;


public interface PipeLineStep {
    String next(String event);
}