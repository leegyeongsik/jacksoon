package io.jacksoon.common.produce.worker;

import java.util.List;

public interface ProduceStore<T> {
    void saveAll(List<T> buffers);
}
