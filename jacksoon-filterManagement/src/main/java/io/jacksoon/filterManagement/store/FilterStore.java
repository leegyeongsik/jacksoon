package io.jacksoon.filterManagement.store;

import io.jacksoon.filterManagement.exception.FilterStoreException;
import io.jacksoon.init.annotation.Init;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

@Init
public class FilterStore {// 여기서 컴파일 저장해놓고
    // 파일을 따로 저장해놔야겠다 버전별로 관리하는 느낌으로
    // 버전명
    // 버전이랑 필터세팅에 버전 저장해놓고 map에 갱신해놓음 그리고 버전묶음을 jar로 통합시키자 디렉토리가 두개 버전별 jar를 버전별 통합 jar들
    // 통합jar는 filtersetting에 있는거 찾아서 만듬
    private final ReentrantLock operationLock = new ReentrantLock(true);
    private volatile State state = new State(0L, null, Map.of());

    public void beginUpdate() {
        operationLock.lock();
    }

    public void completeUpdate() {
        if (!operationLock.isHeldByCurrentThread()) {
            throw new FilterStoreException("Filter update lock is not held by current thread");
        }
        operationLock.unlock();
    }

    public boolean isUpdateLockedByCurrentThread() {
        return operationLock.isHeldByCurrentThread();
    }

    public long version() {
        return state.bundleVersion();
    }

    public Map<String, FilterDefinition> snapshot() {
        return state.activeFilters();
    }

    public Path currentBundle() {
        return state.currentBundle();
    }

    public synchronized void initialize(Map<String, FilterDefinition> activeFilters, long version, Path bundlePath) {
        if (state.bundleVersion() != 0L) {
            throw new FilterStoreException("FilterStore is already initialized");
        }
        validateState(activeFilters, version, bundlePath);
        state = new State(version, bundlePath, Map.copyOf(activeFilters));
    }

    public void commit(Map<String, FilterDefinition> candidateFilters, long nextVersion, Path bundlePath) {
        if (!operationLock.isHeldByCurrentThread()) {
            throw new FilterStoreException("Filter update lock is not held by current thread");
        }
        if (nextVersion != state.bundleVersion() + 1) {
            throw new FilterStoreException("Invalid next filter bundle version. current=" + state.bundleVersion() + ", next=" + nextVersion);
        }
        validateState(candidateFilters, nextVersion, bundlePath);
        state = new State(nextVersion, bundlePath, Map.copyOf(candidateFilters));
    }

    private void validateState(Map<String, FilterDefinition> filters, long version, Path bundlePath) {
        if (version < 1L) {
            throw new FilterStoreException("Filter bundle version must be greater than zero");
        }
        if (filters == null) {
            throw new FilterStoreException("Filter definitions must not be null");
        }
        if (bundlePath == null || !Files.isRegularFile(bundlePath)) {
            throw new FilterStoreException("Filter bundle path is invalid: " + bundlePath);
        }

        for (Map.Entry<String, FilterDefinition> entry : filters.entrySet()) {
            String filterName = entry.getKey();
            FilterDefinition definition = entry.getValue();

            if (filterName == null || filterName.isBlank()) {
                throw new FilterStoreException("Filter name must not be blank");
            }
            if (definition == null || definition.config() == null) {
                throw new FilterStoreException("Filter definition is invalid. filterName=" + filterName);
            }
            if (!filterName.equals(definition.config().filterName())) {
                throw new FilterStoreException("Filter definition name mismatch. filterName=" + filterName);
            }
            if (definition.artifactVersion() < 1L || definition.artifactVersion() > version) {
                throw new FilterStoreException("Invalid filter artifact version. filterName=" + filterName);
            }
            if (definition.jarPath() == null || !Files.isRegularFile(definition.jarPath())) {
                throw new FilterStoreException("Filter jar path is invalid. filterName=" + filterName);
            }
        }
    }

    private record State(
            long bundleVersion,
            Path currentBundle,
            Map<String, FilterDefinition> activeFilters
    ) {
    }
}