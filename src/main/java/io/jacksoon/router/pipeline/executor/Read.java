package io.jacksoon.router.pipeline.executor;

import io.jacksoon.router.pipeline.PipeLineExecutor;

public class Read implements PipeLineExecutor {
    String event;
    @Override
    public void executor() {

    }
    Read(String event){
        this.event = event;
    }
}
