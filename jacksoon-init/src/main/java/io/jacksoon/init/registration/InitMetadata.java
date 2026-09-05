package io.jacksoon.init.registration;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public abstract class InitMetadata{
    String name;
    Class<?> clazz;
    TypeMetadata typeMetadata;

    public InitMetadata(Class<?> clazz,String name){
        this.clazz = clazz;
        this.name =name;
    }
    public abstract InitInstance createInstance();
}
