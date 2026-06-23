package io.jacksoon.router.init.factory;

import io.jacksoon.router.help.HttpRequestCheck;
import io.jacksoon.router.init.annotation.Init;
import io.jacksoon.router.init.registration.*;
import io.jacksoon.router.seletor.Reactor;
import io.jacksoon.router.worker.connection.ConnectionContext;
import io.jacksoon.router.worker.connection.ConnectionQueue;
import io.jacksoon.router.worker.connection.ConnectionWorkerPool;
import io.jacksoon.router.worker.thread.RequestWorkerPool;
import org.reflections.Reflections;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

import static io.jacksoon.router.init.registration.RegistrationScan.createNode;


public class InitFactory {
    static Map<String, InitInstance> initMap = new HashMap<>();
    static Map<Class<?>, List<InitInstance>> typeListMap = new HashMap<>();
    static Map<Class<?>, InitNode> nodeMap = new HashMap<>();
    public static void initialize() {
        init();
    }
    private static void clear() {
        initMap.clear();
        typeListMap.clear();
        nodeMap.clear();
    }
    public static void initialize(Class<?> testClass) {
        clear();

        Class<?>[] classes = testClass.getDeclaredClasses();

        for (Class<?> clazz : classes) {

            Init init = clazz.getAnnotation(Init.class);

            if (init == null) {
                continue;
            }
            String name = init.value();
            if (name.isEmpty()) {
                name = clazz.getSimpleName();
            }
            RegistrationScan.scan(clazz, name);
        }
        for (Class<?> clazz : classes) {
            if (!clazz.isAnnotationPresent(Init.class)) {
                continue;
            }
            createNode(clazz);
        }
        for (InitInstance instance : initMap.values()) {
            instance.getInitMetadata().createInstance();
        }
    }
    private static void init() {
        Reflections reflections = new Reflections("io.jacksoon.router");
        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(io.jacksoon.router.init.annotation.Init.class);
        for (Class<?> clazz : classes) {
            Init init = clazz.getAnnotation(Init.class);
            String name = init.value();
            if (name.isEmpty()) {
                name = clazz.getSimpleName();
            }
            RegistrationScan.scan(clazz, name);
        }

        for (Class<?> aClass : classes) {
            createNode(aClass);
        }

        for (InitInstance initInstance : initMap.values()) {
            initInstance.getInitMetadata().createInstance();
        }
        Reactor backendReactor = InitFactory.get("backendReactor");
        Reactor clientReactor = InitFactory.get("clientReactor");
        RequestWorkerPool requestWorkerPool = InitFactory.get(RequestWorkerPool.class);
        ConnectionWorkerPool connectionWorkerPool = InitFactory.get(ConnectionWorkerPool.class);
        new Thread(clientReactor).start();
        new Thread(backendReactor).start();
        requestWorkerPool.start();
        connectionWorkerPool.start();
        ConnectionQueue connectionQueue = InitFactory.get(ConnectionQueue.class);
        connectionQueue.put(new ConnectionContext("localhost",8080));
    }
    public static InitInstance scanPutClass(Class<?> clazz, String name) {
        InitClassMetadata metadata = new InitClassMetadata(clazz, name);

        metadata.setTypeMetadata(TypeMetadataParser.parse(clazz));

        InitInstance initInstance = new InitInstance(metadata);

        initMap.put(name, initInstance);

        typeListMap.computeIfAbsent(clazz, key -> new ArrayList<>()).add(initInstance);
        Type genericSuper = clazz.getGenericSuperclass();
        if (genericSuper instanceof ParameterizedType pt) {
            TypeMetadata superMeta = TypeMetadataParser.parse(pt);
            typeListMap.computeIfAbsent(superMeta.getRawType(), key -> new ArrayList<>()).add(initInstance);
        }
        return initInstance;
    }

    public static void scanPutMethod(Class<?> clazz, Method method, String name, InitInstance parent) {
        InitMethodMetadata metadata = new InitMethodMetadata(clazz, method ,parent,name);
        metadata.setTypeMetadata(TypeMetadataParser.parse(method.getGenericReturnType()));
        InitInstance initInstance = new InitInstance(metadata);
        initMap.put(name, initInstance);
        Class<?> rawType = metadata.getTypeMetadata().getRawType();
        typeListMap.computeIfAbsent(rawType, key -> new ArrayList<>()).add(initInstance);
    }
    public static InitNode getInitNode(Class<?> clazz){
        return nodeMap.computeIfAbsent(clazz, InitNode::new);
    }
    public static void putInitNode(InitNode child, InitNode parent){
        child.putParent(parent);
        parent.putChildren(child);
    }
    public static boolean isInitNode(Class<?> clazz) {
        return nodeMap.containsKey(clazz);
    }
    public static InitInstance getInitInstance(String name){
        return initMap.get(name);
    }
    public static List<InitInstance> getTypeList(Class<?> clazz){
        return typeListMap.get(clazz);
    }

    public static <T> T get(Class<T> type) {
        for (InitInstance initInstance : initMap.values()) {
            Object object = initInstance.getObject();

            if (type.isAssignableFrom(object.getClass())) {
                return type.cast(object);
            }
        }

        throw new RuntimeException(type.getName() + " not found");
    }
    public static <T> T get(String name) {
        InitInstance initInstance = initMap.get(name);

        if (initInstance == null) {
            throw new RuntimeException("Bean not found : " + name);
        }

        return (T) initInstance.getObject();
    }
}

