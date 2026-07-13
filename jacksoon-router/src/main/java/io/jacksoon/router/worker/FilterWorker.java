package io.jacksoon.router.worker;

import io.jacksoon.router.filter.FilterExecutor;
import io.jacksoon.router.filter.FilterRequestSetting;

public class FilterWorker implements Runnable {
    private final FilterRequestSetting setting;
    private final FilterExecutor executor;
    private final long intervalMillis;

    public FilterWorker(FilterExecutor executor, FilterRequestSetting setting, long intervalMillis) {
        this.executor = executor;
        this.setting = setting;
        this.intervalMillis = intervalMillis;
    }

    @Override
    public void run() { // 주기적으로 요청해서 현재 버전이랑 다르면 요청해서 갱신함
        while (!Thread.currentThread().isInterrupted()) {
            try {
                executor.execute(setting);
                Thread.sleep(intervalMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (RuntimeException e) {
                e.printStackTrace();
                sleepAfterFailure();
            }
        }
    }

    private void sleepAfterFailure() {
        try {
            Thread.sleep(intervalMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
