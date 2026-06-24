package io.jacksoon.router.init.registration;

import io.jacksoon.router.init.annotation.Init;

import java.lang.reflect.Method;

import static io.jacksoon.router.init.factory.InitFactory.*;
public class RegistrationScan {

    public static void scan(Class<?> clazz, String name) {
        InitInstance initInstance = scanPutClass(clazz, name);
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            Init init = method.getAnnotation(Init.class);
            if (init != null) {
                String methodName = init.value();
                if (methodName.isEmpty()) {
                    methodName = method.getName();
                }
                scanPutMethod(clazz, method, methodName, initInstance);
            }
        }
    }
    public static void createNode(Class<?> current) {
        if (isInitNode(current)) {
            return;
        }
        InitNode child = getInitNode(current);
        Class<?> parent = current.getSuperclass();
        if (parent != null && parent != Object.class) {
            createNode(parent);
            putInitNode(child, getInitNode(parent));

        }
        for (Class<?> interfaceClass : current.getInterfaces()) {
            createNode(interfaceClass);
            putInitNode(child, getInitNode(interfaceClass));
        }
    }
}

