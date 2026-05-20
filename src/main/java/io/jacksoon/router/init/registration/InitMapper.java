package io.jacksoon.router.init.registration;

import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Method;

@Getter
public class InitMapper{
    @Setter
    Object object;
    Class<?> clazz;
    String type;
    String parent;
    Method method;
    // 근데 저렇게하면 어케찾지? 찾을수있긴함 역으로 맵핑해둬서 찾고 거기서 다시 name으로 찾으면 되긴하는데
    // 순서가 그럼 패런츠가 존재하면 걔부터 일단 만들고 메서드임 createClass , createMethod 두개 둬서 재귀적으로 굴리면 되겠당 메서드차롄데 class가 없으면 클래스 먼저 만들어오고 메서드 이제 만들고
    public InitMapper(Class<?> clazz, String type){
        this.clazz = clazz;
        this.type = type;
    }
    public InitMapper(Class<?> clazz , String type,Method method,String parent){
        this.clazz = clazz;
        this.type = type;
        this.parent = parent;
        this.method = method;
    }
}
