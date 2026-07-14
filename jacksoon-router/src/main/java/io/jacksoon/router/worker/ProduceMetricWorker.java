package io.jacksoon.router.worker;

import io.jacksoon.common.produce.dto.ProduceDto;
import io.jacksoon.common.util.CommonBlockingQueue;
import io.jacksoon.router.produce.dto.RouterMetricProduceDto;

import java.util.HashMap;
import java.util.Map;

public class ProduceMetricWorker implements Runnable{
    private final Map<String , ServiceMetric> serviceMetricMap = new HashMap<>();
    private final CommonBlockingQueue<ServiceRequest> metricQueue;
    private final CommonBlockingQueue<ProduceDto> produceDtoQueue ;
    private final long MAXIMUM_SIZE = 500;
    long cnt;
    public ProduceMetricWorker(CommonBlockingQueue<ServiceRequest> metricQueue, CommonBlockingQueue<ProduceDto> produceDtoQueue) {
        this.metricQueue = metricQueue;
        this.produceDtoQueue = produceDtoQueue;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                ServiceRequest serviceRequest = metricQueue.poll();
                if (serviceRequest!= null) {
                    Do(serviceRequest);
                }
                boolean is = serviceRequest == null;
                boolean size =  cnt>=MAXIMUM_SIZE;
                if(is || size){
                    DODO();
                }
            }
        } catch (Exception e) {
        } finally {
            DODO();
        }
    }

    private void DODO() {
        for (String s : serviceMetricMap.keySet()) {
            ServiceMetric serviceMetric = serviceMetricMap.get(s);
            if(serviceMetric.success != 0 || serviceMetric.failure != 0){
                RouterMetricProduceDto routerMetricProduceDto =new RouterMetricProduceDto(s,serviceMetric.getSuccess(),serviceMetric.getFailure());
                produceDtoQueue.put(routerMetricProduceDto);
            }
        }
        serviceMetricMap.clear();
        cnt = 0;
    }

    void Do(ServiceRequest serviceRequest){
        serviceMetricMap.putIfAbsent(serviceRequest.getServiceName(),new ServiceMetric(0,0));
        ServiceMetric serviceMetric = serviceMetricMap.get(serviceRequest.getServiceName());
        serviceMetric.update(serviceRequest.isSuccess);
        cnt+=1;
    }
}
