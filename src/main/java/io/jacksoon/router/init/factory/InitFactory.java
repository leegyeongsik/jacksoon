package io.jacksoon.router.init.factory;

import io.jacksoon.router.init.annotation.Init;
import io.jacksoon.router.init.registration.InitMapper;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static io.jacksoon.router.init.registration.Registration.create;
import static io.jacksoon.router.init.registration.Registration.scan;

public class InitFactory {
    static Map<String, InitMapper> initMap = new HashMap<>();
    public static void initialize() {
        init();
    }
    private static void init() {
        Reflections reflections = new Reflections("io.jacksoon.router");
        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(
                io.jacksoon.router.init.annotation.Init.class
        );


        for (Class<?> clazz : classes) {
            Init init = clazz.getAnnotation(Init.class);
            String name = init.value();
            if (name.isEmpty()) {
                name = clazz.getSimpleName();
            }
            scan(clazz,name);
            for (String identifier : initMap.keySet()) {
                try {
                    create(initMap.get(identifier));
                } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public static InitMapper get(String name) {
        return initMap.get(name);
    }
    public static void scanPutClass(Class<?> clazz, String name) {
        initMap.put(name,new InitMapper(clazz,"CLASS"));
    }
    public static void scanPutMethod(Class<?> clazz, Method method, String name,String parent) {
        initMap.put(name,new InitMapper(clazz,"METHOD",method,parent));
    }
}
