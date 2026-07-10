package io.jacksoon.filterManagement.pipeline.task;

import io.jacksoon.filterManagement.pipeline.context.FilterPipelineContext;
import io.jacksoon.filterManagement.pipeline.depth.FilterDepth;
import io.jacksoon.filterManagement.store.FilterStore;
import io.jacksoon.init.annotation.Init;

@Init
public class FilterCreate implements FilterDepth { // 세팅대로 버전에 있는거 묶어서 jar로 하면되네
    private final FilterStore filterStore;
    public FilterCreate(FilterStore filterStore) {
        this.filterStore = filterStore;
    }

    @Override
    public void dodo(FilterPipelineContext context) {
        // 저장할때 어떻게할까 어차피 jar가 디렉토리에 있으니까 이름으로 가져오면되고
        // 스토어 갱신하고 여기서 그냥 current 버전 jar 통합
        // 여기서 세팅 갱신하고 번들로 넘기셈

    }

    @Override
    public String currentEvent() {
        return "";
    }
}
