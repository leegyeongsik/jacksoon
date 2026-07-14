package io.jacksoon.router.worker;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ServiceMetric {
    long success;
    long failure;

    public void update(boolean isSuccess){
        if(isSuccess){
            success+=1;
        }else {
            failure+=1;
        }
    }
}
