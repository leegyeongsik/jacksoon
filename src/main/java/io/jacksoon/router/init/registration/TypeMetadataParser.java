package io.jacksoon.router.init.registration;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.List;

public class TypeMetadataParser {
    public static TypeMetadata parse(Type type) {

        if (type instanceof ParameterizedType pt) {
            Class<?> rawType = (Class<?>) pt.getRawType();

            List<TypeMetadata> args = new ArrayList<>();
            for (Type arg : pt.getActualTypeArguments()) {
                args.add(parse(arg));
            }

            return new TypeMetadata(type, rawType, args, false, List.of());
        }

        if (type instanceof WildcardType wt) {
            List<TypeMetadata> upper = new ArrayList<>();
            for (Type b : wt.getUpperBounds()) {
                upper.add(parse(b));
            }

            return new TypeMetadata(type, null, List.of(), true, upper);
        }

        if (type instanceof TypeVariable<?> tv) {
            List<TypeMetadata> bounds = new ArrayList<>();
            for (Type b : tv.getBounds()) {
                bounds.add(parse(b));
            }

            return new TypeMetadata(type, null, List.of(), false, bounds);
        }

        if (type instanceof Class<?> clazz) {

            List<TypeMetadata> vars = new ArrayList<>();
            for (TypeVariable<?> tv : clazz.getTypeParameters()) {
                List<TypeMetadata> bounds = new ArrayList<>();
                for (Type b : tv.getBounds()) {
                    bounds.add(parse(b));
                }
                vars.add(new TypeMetadata(tv, null, List.of(), false, bounds));
            }

            return new TypeMetadata(type, clazz, vars, false, List.of());
        }

        throw new IllegalStateException("Unsupported type: " + type);
    }

}