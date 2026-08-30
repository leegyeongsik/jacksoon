package io.jacksoon.router.worker;

import io.jacksoon.common.exception.ExceptionDispatcher;
import io.jacksoon.common.produce.dto.ProduceDto;
import io.jacksoon.common.util.CommonBlockingQueue;
import io.jacksoon.router.exception.RouterMetricException;
import io.jacksoon.router.produce.dto.ServiceMetric;
import io.jacksoon.router.produce.dto.ServiceRequest;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class ProduceMetricWorker implements Runnable {
    private final Map<String, ServiceMetric> serviceMetricMap = new HashMap<>();
    private final CommonBlockingQueue<ServiceRequest> metricQueue;
    private final CommonBlockingQueue<ProduceDto> produceDtoQueue;
    private final long MAXIMUM_SIZE = 500;
    private final Constructor<?> constructor;
    private final ExceptionDispatcher exceptionDispatcher;
    long cnt;
    public ProduceMetricWorker(CommonBlockingQueue<ServiceRequest> metricQueue, CommonBlockingQueue<ProduceDto> produceDtoQueue, Class<?> clazz, ExceptionDispatcher exceptionDispatcher) {
        this.metricQueue = metricQueue;
        this.produceDtoQueue = produceDtoQueue;
        this.exceptionDispatcher = exceptionDispatcher;
        try {
            this.constructor = clazz.getConstructor(String.class, long.class, long.class);
        } catch (NoSuchMethodException e) {
            throw new RouterMetricException("Metric DTO constructor not found. class=" + clazz.getName(), e);
        }
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    ServiceRequest serviceRequest = metricQueue.poll();
                    if (serviceRequest != null) {
                        Do(serviceRequest);
                    }
                    boolean empty = serviceRequest == null;
                    boolean size = cnt >= MAXIMUM_SIZE;
                    if (empty || size) {
                        DODO();
                    }
                } catch (Exception e) {
                    exceptionDispatcher.dispatch(e);
                }
            }
        } finally {
            try {
                DODO();
            } catch (Exception e) {
                exceptionDispatcher.dispatch(e);
            }
        }
    }

    private void DODO() {
        for (String serviceName : serviceMetricMap.keySet()) {
            ServiceMetric serviceMetric = serviceMetricMap.get(serviceName);
            if (serviceMetric.success != 0 || serviceMetric.failure != 0) {
                try {
                    produceDtoQueue.put((ProduceDto) constructor.newInstance(serviceName, serviceMetric.getSuccess(), serviceMetric.getFailure()));
                } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
                    throw new RouterMetricException("Failed to create metric dto. serviceName=" + serviceName, e);
                }
            }
        }
        serviceMetricMap.clear();
        cnt = 0;
    }

    void Do(ServiceRequest serviceRequest) {
        serviceMetricMap.putIfAbsent(serviceRequest.getServiceName(), new ServiceMetric(0, 0));
        ServiceMetric serviceMetric = serviceMetricMap.get(serviceRequest.getServiceName());
        serviceMetric.update(serviceRequest.isSuccess);
        cnt += 1;
    }
}
