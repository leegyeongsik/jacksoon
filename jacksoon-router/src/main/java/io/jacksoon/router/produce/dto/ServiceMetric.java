package io.jacksoon.router.produce.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ServiceMetric {
    public long success;
    public long failure;

    public void update(boolean isSuccess){
        if(isSuccess){
            success+=1;
        }else {
            failure+=1;
        }
    }
}
