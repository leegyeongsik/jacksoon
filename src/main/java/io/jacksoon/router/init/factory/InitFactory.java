package io.jacksoon.router.init.factory;

import io.jacksoon.router.init.annotation.Init;
import org.reflections.Reflections;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static io.jacksoon.router.init.registration.Registration.create;

public class InitFactory {
    static Map<Class<?>, Set<String>> typeMap = new HashMap<>();
    static Map<String, Object> nameMap = new HashMap<>();
    public static void initialize() {
        if (!typeMap.isEmpty()) {
            return;
        }
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
            try {
                create(clazz, name);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static Object get(Class<?> clazz,String name) {
        if(!typeMap.containsKey(clazz)){
            return null;
        }
        if(!typeMap.get(clazz).contains(name)){
            return null;
        }
        return nameMap.get(name);
    }
    public static void put(Object object, String name,Class<?>clazz) {
        Set<String> set;
        if (!typeMap.containsKey(clazz)) {
            set = new HashSet<>();
            typeMap.put(clazz, set);
        } else {
            set = typeMap.get(clazz);
        }
        set.add(name);
        nameMap.put(name, object);
    }
}
