package io.jacksoon.router.init.registration;

import io.jacksoon.router.init.annotation.Init;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

import static io.jacksoon.router.init.factory.InitFactory.get;
import static io.jacksoon.router.init.factory.InitFactory.put;

public class Registration {
    static public Object create(Class clazz, String name) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        if (name.isEmpty()) {
            name = clazz.getSimpleName();
        }
        if (get(clazz, name) != null) {
            return get(clazz, name);
        }
        Constructor<?> constructor = clazz.getConstructors()[0];
        Class<?>[] ca = constructor.getParameterTypes();
        Object[] objects = new Object[ca.length]; // 이렇게했을때 만약에 생성을 메서드로 하면 걔의 생성자가 있겠지? 근데 그걸로 생성하지않고 메서드로 생성하는게 맞는거일수있는데
        for (int i = 0; i < objects.length; i++) { //이렇게하면 걔의 생성자로 생성해서 찐빠가 날수있다
                                                    // 그래서 스캔을 하면 걔 파라미터는 어디에있고 걔는 어떻게 만들어야한다 라는게 정의가 되어있기때문에 잘 만들듯?
            Init init = constructor.getParameters()[i].getAnnotation(Init.class);
            String paraName;
            paraName = (Objects.requireNonNull(init).value());
            if (paraName.isEmpty()) {
                paraName = ca[i].getSimpleName();
            }
            if (get(ca[i], paraName) == null) {
                objects[i] = create(ca[i], paraName);
            } else {
                objects[i] = get(ca[i], paraName);
            }
        }
        Object instance = constructor.newInstance(objects);
        put(instance, name, clazz);
        return get(clazz, name);
    }
}
