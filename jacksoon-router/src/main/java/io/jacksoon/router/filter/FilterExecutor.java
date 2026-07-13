package io.jacksoon.router.filter;

import io.jacksoon.common.worker.Executor;
import io.jacksoon.init.annotation.Init;

@Init
public class FilterExecutor implements Executor<FilterRequestSetting> {
    private final FilterLoad filterLoad;

    public FilterExecutor( FilterLoad filterLoad) {
        this.filterLoad = filterLoad;
    }
    // 엔드포인트 요청 하고
    // 만약에 값이 다르면

    @Override
    public void execute(FilterRequestSetting setting) {
        long remoteVersion = filterLoad.requestVersion(setting.versionUri().toString());
        if (remoteVersion == setting.version()) {
            return;
        }
        if (remoteVersion == 0L) {
            setting.updateVersion(0L);
            return;
        }

        long loadedVersion = filterLoad.loadAndReplace(setting.bundleUri().toString());
        if (loadedVersion != remoteVersion) {
            throw new IllegalArgumentException();
        }
        setting.updateVersion(loadedVersion);
    }
}
