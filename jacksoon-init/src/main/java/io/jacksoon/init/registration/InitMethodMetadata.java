package io.jacksoon.init.registration;

import io.jacksoon.init.annotation.Init;
import io.jacksoon.init.factory.InitFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

public class InitMethodMetadata extends InitMetadata {
    Method method;
    InitInstance parent;

    public InitMethodMetadata(Class<?> clazz, Method method, InitInstance parent, String name) {
        super(clazz, name);
        this.clazz = clazz;
        this.parent = parent;
        this.method = method;
    }

    @Override
    public InitInstance createInstance() {
        InitInstance initInstance = InitFactory.getInitInstance(name);
        if (initInstance.getObject() != null) {
            return initInstance;
        }
        Object parentObject = parent.getInitMetadata().createInstance().getObject();
        Method method = this.method;
        Type[] genericParams = method.getGenericParameterTypes();
        Class<?>[] rawParams = method.getParameterTypes();
        Object[] args = new Object[rawParams.length];
        for (int i = 0; i < args.length; i++) {
            String paramName = null;
            Init init = method.getParameters()[i].getAnnotation(Init.class);
            if (init != null && !init.value().isEmpty()) {
                paramName = init.value();
            }
            TypeMetadata metadata = TypeMetadataParser.parse(genericParams[i]);
            args[i] = InitProcess.resolve(metadata, paramName);
        }
        try {
            initInstance.setObject(method.invoke(parentObject, args));
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        return initInstance;

    }
}
