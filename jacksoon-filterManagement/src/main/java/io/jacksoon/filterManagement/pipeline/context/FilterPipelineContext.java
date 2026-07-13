package io.jacksoon.filterManagement.pipeline.context;

import io.jacksoon.common.filter.FilterUploadRequest;
import io.jacksoon.common.pipeline.context.PipelineContext;
import io.jacksoon.common.util.BufferContext;
import io.jacksoon.filterManagement.store.FilterDefinition;
import lombok.Getter;
import lombok.Setter;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.util.Map;
@Getter
@Setter
public class FilterPipelineContext extends PipelineContext {
    private FilterUploadRequest filterUploadRequest;
    private String filterName;
    private long operationVersion;
    private Path artifactPath;
    private Map<String, FilterDefinition> candidateFilters;
    private boolean updateLockHeld;

    public FilterPipelineContext(SocketChannel socketChannel, String event, ByteBuffer byteBuffer, int byteBufferIndex, BufferContext bufferContext, SelectionKey selectionKey) {
        super(socketChannel, event, byteBuffer, byteBufferIndex, bufferContext, selectionKey);
    }
}
