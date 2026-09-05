package io.jacksoon.router.pipeline.executor;

import io.jacksoon.common.filter.FilterTiming;
import io.jacksoon.common.filter.PipelineType;
import io.jacksoon.common.pipeline.executor.PipelineDispatcher;
import io.jacksoon.common.pipeline.executor.PipelineExecutor;
import io.jacksoon.common.pipeline.executor.PipelineExecutorRegistry;
import io.jacksoon.router.exception.RouterFilterExecutionException;
import io.jacksoon.router.filter.FilterRegistry;
import io.jacksoon.router.pipeline.context.RouterPipelineContext;

public class RouterPipelineDispatcher implements PipelineDispatcher<RouterPipelineContext> {
    private final PipelineExecutorRegistry<RouterPipelineContext> executorRegistry;
    private final FilterRegistry filterRegistry;

    public RouterPipelineDispatcher(PipelineExecutorRegistry<RouterPipelineContext> executorRegistry, FilterRegistry filterRegistry) {
        this.executorRegistry = executorRegistry;
        this.filterRegistry = filterRegistry;
    }

    @Override
    public void dispatch(RouterPipelineContext context) {
        String currentEvent = context.getEvent();
        PipelineType pipelineType = PipelineType.fromEvent(currentEvent);
        PipelineExecutor<RouterPipelineContext> executor = executorRegistry.get(currentEvent);
        filterRegistry.execute(FilterTiming.PRE, pipelineType, context); // pre , router 뎁스에 등록된 필터가 a, b 두개가 있으면 등록된 pre + router뎁스의 필터들을 실행함
        if (!currentEvent.equals(context.getEvent())) {
            return;
        }
        executor.execute(context);
        try {
            filterRegistry.execute(FilterTiming.POST, pipelineType, context);
        } catch (RouterFilterExecutionException e) {
            if (pipelineType != PipelineType.ROUTING) {
                throw e;
            }
        }
        if (currentEvent.equals(context.getEvent())) {
            context.setEvent(executor.nextEvent());
        }
    }
}
