package io.jacksoon.router.init.registration;

import io.jacksoon.router.init.annotation.Init;
import io.jacksoon.router.init.factory.InitFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static io.jacksoon.router.init.registration.InitProcess.resolve;

public class InitClassMetadata extends InitMetadata{
    public InitClassMetadata(Class<?> clazz, String name) {
        super(clazz,name);
    }

    @Override
    public InitInstance createInstance() {
        InitInstance initInstance = InitFactory.getInitInstance(name);
        if(initInstance.getObject()!=null){
            return initInstance;
        }
        Constructor<?> constructor = this.clazz.getConstructors()[0];
        Class<?>[] ca = constructor.getParameterTypes();
        Object[] objects = new Object[ca.length];
        for (int i = 0; i < objects.length; i++) {
            Init init = constructor.getParameters()[i].getAnnotation(Init.class);
            String name = init == null ? null : init.value();
            objects[i] = resolve(TypeMetadataParser.parse(constructor.getGenericParameterTypes()[i]),name);
        }
        try {
            initInstance.setObject(constructor.newInstance(objects));
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        return initInstance;
    }
}
