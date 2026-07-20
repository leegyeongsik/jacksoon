package io.jacksoon.router.produce.dto;

import lombok.Getter;

@Getter
public class ServiceRequest {
    public String serviceName;
    public boolean isSuccess;
    public ServiceRequest(String serviceName , boolean isSuccess){
        this.serviceName = serviceName;
        this.isSuccess = isSuccess;
    }
}
