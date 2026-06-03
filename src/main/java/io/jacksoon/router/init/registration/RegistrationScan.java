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
        // 노드를 일단 생성을 한다고 했을때

        // 여기서 부모 자식 관계 다 넣어줌
        // 그치 맞네 그러면 안에다가 부모자식을 넣을이유가있나? 그냥 찾아서 내려가게끔 하면되는거아님?
        // 클래스의 부모자식관계를 정립하고
        // 일단 꼭대기에서 출발해서 찾고 자식 들어가서 찾고 그런거니까


        // 1. class 찾아서 거기서 타고 들어가고
        // 2. static Map<Class<?>, List<InitInstance>> typeListMap = new HashMap<>(); 찾은거 리스트업
        // 3. Set<InitNode> children; 자식한번 호출하고
        // 4. 거기로 들어가서 리스트업
        // 5. 리스트업된거에서 매칭할거 체크해서 주입
        // 하나 집어서 위쪽으로 올라갑시다

        // 근데 방문처리같은거 염두


}

