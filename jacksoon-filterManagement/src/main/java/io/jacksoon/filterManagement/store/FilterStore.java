package io.jacksoon.filterManagement.store;

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
            throw new IllegalArgumentException();
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

    public void commit(Map<String, FilterDefinition> candidateFilters, long nextVersion, Path bundlePath) {
        if (!operationLock.isHeldByCurrentThread()) {
            throw new IllegalArgumentException();
        }
        if (nextVersion != state.bundleVersion() + 1) {
            throw new IllegalArgumentException();
        }
        if (candidateFilters == null) {
            throw new IllegalArgumentException();
        }
        if (bundlePath == null || !Files.isRegularFile(bundlePath)) {
            throw new IllegalArgumentException();
        }

        state = new State(nextVersion, bundlePath, Map.copyOf(candidateFilters));
    }

    private record State(
            long bundleVersion,
            Path currentBundle,
            Map<String, FilterDefinition> activeFilters
    ) {
    }
}