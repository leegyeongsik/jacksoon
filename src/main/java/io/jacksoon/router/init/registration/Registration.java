package io.jacksoon.router.init.registration;

import io.jacksoon.router.init.annotation.Init;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

import static io.jacksoon.router.init.factory.InitFactory.*;

public class Registration {
    static public InitMapper create(InitMapper initMapper) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        if (initMapper.getObject()!=null) {
            return initMapper;
        }
        if(Objects.equals(initMapper.getType(), "CLASS")){
            Constructor<?> constructor = initMapper.getClazz().getConstructors()[0];
            Class<?>[] ca = constructor.getParameterTypes();
            Object[] objects = new Object[ca.length];
            for (int i = 0; i < objects.length; i++) {
                Init init = constructor.getParameters()[i].getAnnotation(Init.class);  // 생성자 파라미터 만들어오셈
                String parameterName;
                if (init != null && !init.value().isEmpty()) {
                    parameterName = init.value();
                } else {
                    parameterName = ca[i].getSimpleName();
                }
                InitMapper classInitMapper = get(parameterName);
                objects[i] = create(classInitMapper).getObject();
            }
            Object instance = constructor.newInstance(objects);
            initMapper.setObject(instance);
        }else {
            Object parent = create(get(initMapper.getParent())).getObject(); // 부모부터 만들고 파라미터 만드셈
            Method method =  initMapper.getMethod();

            Class<?>[] para = method.getParameterTypes();
            Object[] objects = new Object[para.length];
            for (int i = 0; i < objects.length; i++) { // 부모만들었으면 메서드 파라미터 만들어오셈
                Init init = method.getParameters()[i].getAnnotation(Init.class);
                String parameterName;
                if (init != null && !init.value().isEmpty()) {
                    parameterName = init.value();
                } else {
                    parameterName = para[i].getSimpleName();
                }
                objects[i] = create(get(parameterName)).getObject();
            }
            initMapper.setObject(method.invoke(parent,objects));
        }
        return initMapper;
    }
    static public void scan(Class<?> clazz,String name){

        scanPutClass(clazz,name);
        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            Init init = method.getAnnotation(Init.class);
            if(init!=null){
               String methodName = init.value();
               if(methodName.isEmpty()){
                   methodName = method.getName();
               }
               scanPutMethod(clazz,method,methodName,name);
            }
        }
    }

}
