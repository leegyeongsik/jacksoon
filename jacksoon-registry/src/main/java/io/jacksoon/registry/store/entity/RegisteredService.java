package io.jacksoon.registry.store.entity;

import io.jacksoon.common.produce.dto.ProduceDto;
import io.jacksoon.common.produce.dto.ProduceHint;
import io.jacksoon.common.produce.dto.ProducerType;
import io.jacksoon.common.util.CommonBlockingQueue;
import io.jacksoon.registry.dto.produce.RegistryAction;
import io.jacksoon.registry.dto.produce.RegistryProduceDto;
import lombok.Getter;

import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class RegisteredService {
    private final CommonBlockingQueue<ProduceDto> produceDtoQueue;
    private final String serviceName;
    private final Map<String, RegisteredEndpoint> endpointMap = new ConcurrentHashMap<>();

    public RegisteredService(String serviceName, CommonBlockingQueue<ProduceDto> produceDtoQueue) {
        this.produceDtoQueue = produceDtoQueue;
        this.serviceName = serviceName;
    }


    public void putEndpoint(RegisteredEndpoint endpoint) {
        endpointMap.put(endpoint.getInstanceId(), endpoint);
    }

    public Collection<RegisteredEndpoint> endpoints() {
        return endpointMap.values();
    }

    public void removeEndpoint(String instanceId) {
        RegisteredEndpoint endpoint = endpointMap.remove(instanceId);
        if (endpoint == null) {
            return;
        }
        produceDtoQueue.put(new RegistryProduceDto(RegistryAction.REMOVE, endpoint, "unconnection-service", ProduceHint.SERVICE, ProducerType.REGISTRY, Instant.now()));
    }

    public void successEndPoint(String instanceId) {
        RegisteredEndpoint endpoint = endpointMap.get(instanceId);
        endpoint.setStatus("active");
        produceDtoQueue.put(new RegistryProduceDto(RegistryAction.REGISTER, endpoint, "active-service", ProduceHint.SERVICE, ProducerType.REGISTRY, Instant.now()));
    }
}