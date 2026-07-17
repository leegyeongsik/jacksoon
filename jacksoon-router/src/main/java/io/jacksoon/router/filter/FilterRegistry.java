package io.jacksoon.router.filter;

import io.jacksoon.common.filter.FilterRegistryKey;
import io.jacksoon.common.filter.FilterTiming;
import io.jacksoon.common.filter.PipelineType;
import io.jacksoon.init.annotation.Init;
import io.jacksoon.router.pipeline.context.RouterPipelineContext;
import lombok.Getter;

import java.io.IOException;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Getter
@Init
public class FilterRegistry {
    private volatile LoadedFilterBundle current = new LoadedFilterBundle(0L, null, null, Map.of());
    public List<RegisteredFilter> get(FilterTiming timing, PipelineType pipeline) {
        LoadedFilterBundle snapshot = current;
        return snapshot.filters().getOrDefault(new FilterRegistryKey(timing, pipeline), List.of()); // 레코드는 equlas()랑 hashcode() 값 자동으로 생성해줘서
                                                                                                    //오브젝트 안에있는 값이 a , b라고했을때 파라미터로 a , b 를 주면 그 key 벨류를 줌
                                                                                                    // 그래서 타이밍이랑 파이프라인에 해당하는 필터를 준다
    }
    public void execute(FilterTiming timing, PipelineType pipeline, RouterPipelineContext context) {
        LoadedFilterBundle snapshot = current;
        List<RegisteredFilter> filters = snapshot.filters().getOrDefault(new FilterRegistryKey(timing, pipeline), List.of());
        for (RegisteredFilter filter : filters) {
            if(filter.filter().isSupport(context)){
                filter.filter().doFilter(context);
            }
        }
    }

    public synchronized void replace(LoadedFilterBundle candidate) {
        Objects.requireNonNull(candidate);
        LoadedFilterBundle previous = current;
        current = candidate;
        closeClassLoader(previous.classLoader());
    }

    private void closeClassLoader(URLClassLoader classLoader) {
        if (classLoader == null) {
            return;
        }
        try {
            classLoader.close();
        } catch (IOException ignored) {
        }
    }
}
