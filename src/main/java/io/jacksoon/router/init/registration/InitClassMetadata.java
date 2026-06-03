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
            // parameter찾을때 후보 갖고와야대고
            // 파라미터가 list map 이런식으로 계층적으로 있으면 파고파고파고들어가서 만들어와야댐
            // 그리고 제네릭같은게 붙어있으면 내 info로 만들어와야댐
            // 근데 제네릭정보 어케쓸거냐 이거는 일단
            // 내가 t면
            // 일단 갖고오고 그 정보를 대입시키는거고
            // 어쨋든 후보를 들고오는거임 후보를 들고올때 제네릭에 대한 정보가 있으면 거기따가 대입시키는거고 없으면 마는거임
            // 일단은 그러면 클래스에 제네릭 정보가 있으면 요청 메타데이터에 치환하고
            // 없으면 그냥 요청 메타데이터 갖고옴
            // 담을수있는 타입이면 뒤에서부터 만들어옴
            // 객체면 그냥 후보갖고옴
            // 핵심은 객체도 메타데이터가 있을텐데 그 메타데이터와 요청 메타데이터를 비교해서 후보 선택함
            // 즉 객체 타입이면 거기서부터 요청데이터타입이랑 가져온 후보 메타데이터랑 비교해서 후보 선택함
            // 담는객체면 수행한거 담음
            // 갖고와서 처리 ㄱㄱ
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
