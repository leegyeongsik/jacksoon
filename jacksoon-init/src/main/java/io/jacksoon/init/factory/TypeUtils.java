package io.jacksoon.init.factory;


import io.jacksoon.init.registration.InitInstance;
import io.jacksoon.init.registration.TypeMetadata;
import io.jacksoon.init.registration.TypeMetadataParser;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TypeUtils {

    public static boolean match(TypeMetadata source, TypeMetadata target) {

        if (source == null || target == null) {
            return false;
        }

        if (target.getSourceType() instanceof TypeVariable<?>) {
            return matchTypeVariable(source, target);
        }

        if (target.isWildcard()) {
            return matchWildcard(source, target);
        }

        if (source.getRawType() == null || target.getRawType() == null) {
            return false;
        }

        if (!target.getRawType().isAssignableFrom(source.getRawType())) {
            return false;
        }

        if (!source.getRawType().equals(target.getRawType())) {
            TypeMetadata convertedSource = toTargetView(source, target.getRawType());

            if (convertedSource == null) {
                return false;
            }

            return match(convertedSource, target);
        }

        List<TypeMetadata> sourceArgs = source.getActualTypeArguments();
        List<TypeMetadata> targetArgs = target.getActualTypeArguments();

        if (targetArgs.isEmpty()) {
            return true;
        }

        if (sourceArgs.isEmpty()) {
            return false;
        }

        if (sourceArgs.size() != targetArgs.size()) {
            return false;
        }

        for (int i = 0; i < sourceArgs.size(); i++) {
            if (!match(sourceArgs.get(i), targetArgs.get(i))) {
                return false;
            }
        }

        return true;
    }

    private static boolean matchTypeVariable(TypeMetadata source, TypeMetadata target) {
        List<TypeMetadata> bounds = target.getUpperBounds();

        if (bounds.isEmpty()) {
            return true;
        }

        for (TypeMetadata bound : bounds) {
            if (bound.getRawType() == Object.class) {
                continue;
            }

            if (!match(source, bound)) {
                return false;
            }
        }

        return true;
    }

    private static boolean matchWildcard(TypeMetadata source, TypeMetadata target) {
        List<TypeMetadata> bounds = target.getUpperBounds();

        if (bounds.isEmpty()) {
            return true;
        }

        for (TypeMetadata bound : bounds) {
            if (bound.getRawType() == Object.class) {
                continue;
            }

            if (!match(source, bound)) {
                return false;
            }
        }

        return true;
    }

    private static TypeMetadata toTargetView(TypeMetadata source, Class<?> targetRawType) {
        if (source == null || source.getRawType() == null || targetRawType == null) {
            return null;
        }

        if (source.getRawType().equals(targetRawType)) {
            return source;
        }

        if (!targetRawType.isAssignableFrom(source.getRawType())) {
            return null;
        }

        Map<TypeVariable<?>, TypeMetadata> bindings = createTypeVariableBindings(source);

        List<Type> parentTypes = new ArrayList<>();

        Type genericSuperclass = source.getRawType().getGenericSuperclass();
        if (genericSuperclass != null && genericSuperclass != Object.class) {
            parentTypes.add(genericSuperclass);
        }

        parentTypes.addAll(Arrays.asList(source.getRawType().getGenericInterfaces()));

        for (Type parentType : parentTypes) {
            TypeMetadata parentMetadata = TypeMetadataParser.parse(parentType);
            TypeMetadata substitutedParent = substitute(parentMetadata, bindings);

            TypeMetadata result = toTargetView(substitutedParent, targetRawType);

            if (result != null) {
                return result;
            }
        }

        return null;
    }

    private static Map<TypeVariable<?>, TypeMetadata> createTypeVariableBindings(TypeMetadata source) {
        Map<TypeVariable<?>, TypeMetadata> bindings = new HashMap<>();

        if (source.getRawType() == null) {
            return bindings;
        }

        TypeVariable<?>[] variables = source.getRawType().getTypeParameters();
        List<TypeMetadata> actualTypes = source.getActualTypeArguments();

        int size = Math.min(variables.length, actualTypes.size());

        for (int i = 0; i < size; i++) {
            bindings.put(variables[i], actualTypes.get(i));
        }

        return bindings;
    }

    private static TypeMetadata substitute(
            TypeMetadata metadata,
            Map<TypeVariable<?>, TypeMetadata> bindings
    ) {
        if (metadata == null) {
            return null;
        }

        if (metadata.getSourceType() instanceof TypeVariable<?> typeVariable) {
            TypeMetadata resolved = bindings.get(typeVariable);

            if (resolved != null) {
                return resolved;
            }

            return metadata;
        }

        List<TypeMetadata> substitutedArgs = new ArrayList<>();
        for (TypeMetadata arg : metadata.getActualTypeArguments()) {
            substitutedArgs.add(substitute(arg, bindings));
        }

        List<TypeMetadata> substitutedBounds = new ArrayList<>();
        for (TypeMetadata bound : metadata.getUpperBounds()) {
            substitutedBounds.add(substitute(bound, bindings));
        }

        return new TypeMetadata(
                metadata.getSourceType(),
                metadata.getRawType(),
                substitutedArgs,
                metadata.isWildcard(),
                substitutedBounds
        );
    }

    private static InitInstance findExactMatch(TypeMetadata request, List<InitInstance> candidates) {
        for (InitInstance candidate : candidates) {
            TypeMetadata candidateType = candidate.getInitMetadata().getTypeMetadata();

            if (request.equals(candidateType)) {
                return candidate;
            }
        }

        return null;
    }

    public static boolean moreSpecific(TypeMetadata left, TypeMetadata right) {
        if (left == null || right == null) {
            return false;
        }

        if (left.equals(right)) {
            return false;
        }

        if (left.isWildcard() && !right.isWildcard()) {
            return false;
        }

        if (!left.isWildcard() && right.isWildcard()) {
            return true;
        }

        if (left.getSourceType() instanceof TypeVariable<?> &&
                !(right.getSourceType() instanceof TypeVariable<?>)) {
            return false;
        }

        if (!(left.getSourceType() instanceof TypeVariable<?>) &&
                right.getSourceType() instanceof TypeVariable<?>) {
            return true;
        }

        if (left.getRawType() == null || right.getRawType() == null) {
            return false;
        }

        if (!left.getRawType().equals(right.getRawType())) {
            if (right.getRawType().isAssignableFrom(left.getRawType())) {
                return true;
            }

            return false;
        }

        List<TypeMetadata> leftArgs = left.getActualTypeArguments();
        List<TypeMetadata> rightArgs = right.getActualTypeArguments();

        if (leftArgs.size() != rightArgs.size()) {
            return false;
        }

        for (int i = 0; i < leftArgs.size(); i++) {
            if (moreSpecific(leftArgs.get(i), rightArgs.get(i))) {
                return true;
            }
        }

        return false;
    }

    public static InitInstance selectBestCandidate(TypeMetadata request, List<InitInstance> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return null;
        }

        InitInstance exact = findExactMatch(request, candidates);

        if (exact != null) {
            return exact;
        }

        InitInstance best = candidates.get(0);

        for (InitInstance candidate : candidates) {
            TypeMetadata candidateType = candidate.getInitMetadata().getTypeMetadata();
            TypeMetadata bestType = best.getInitMetadata().getTypeMetadata();

            if (TypeUtils.moreSpecific(candidateType, bestType)) {
                best = candidate;
            }
        }

        return best;
    }
}