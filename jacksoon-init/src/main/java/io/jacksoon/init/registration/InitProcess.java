package io.jacksoon.init.registration;


import io.jacksoon.init.factory.InitFactory;
import io.jacksoon.init.factory.TypeUtils;

import java.util.ArrayList;
import java.util.List;

import static io.jacksoon.init.factory.TypeUtils.match;

public class InitProcess {
    public static Object resolve(TypeMetadata metadata, String name) {
        if (name != null && !name.isEmpty()) {
            return resolveByName(metadata, name);
        }
        return resolveByType(metadata);
    }

    private static Object resolveByName(TypeMetadata metadata, String name) {
        InitInstance instance = InitFactory.getInitInstance(name);
        if (instance == null) {
            throw new RuntimeException();
        }
        TypeMetadata candidate = instance.getInitMetadata().getTypeMetadata();
        if (!match(candidate, metadata)) {
            throw new RuntimeException();
        }
        return instance.getInitMetadata().createInstance().getObject();
    }

    private static Object resolveByType(TypeMetadata metadata) {
        Class<?> raw = metadata.getRawType();

        if (isContainerType(raw)) {
            return resolveContainer(metadata);
        }

        return resolveObject(metadata);
    }

    private static Object resolveObject(TypeMetadata metadata) {
        List<InitInstance> confirmed = findMatchedCandidates(metadata);
        if (confirmed.isEmpty()) {
            throw new RuntimeException("No candidate: " + metadata);
        }
        return TypeUtils.selectBestCandidate(metadata, confirmed)
                .getInitMetadata()
                .createInstance()
                .getObject();
    }

    private static Object resolveContainer(TypeMetadata metadata) {
        Class<?> raw = metadata.getRawType();
        if (raw == List.class) {
            return resolveList(metadata);
        }
        throw new RuntimeException("Unsupported container type: " + metadata);
    }

    private static Object resolveList(TypeMetadata metadata) {
        TypeMetadata child = metadata.getActualTypeArguments().isEmpty()
                ? new TypeMetadata(Object.class, Object.class, List.of(), false, List.of())
                : metadata.getActualTypeArguments().getFirst();
        if (child.isWildcard() && !child.getUpperBounds().isEmpty()) {
            child = child.getUpperBounds().getFirst();
        }

        List<Object> result = new ArrayList<>();
        if (isContainerType(child.getRawType())) {
            result.add(resolveByType(child));
            return result;
        }
        List<InitInstance> candidates = findMatchedCandidates(child);
        for (InitInstance instance : candidates) {
            result.add(instance.getInitMetadata().createInstance().getObject());
        }
        return result;
    }

    private static boolean isContainerType(Class<?> raw) {
        return raw == List.class;
    }

    private static List<InitInstance> findMatchedCandidates(TypeMetadata metadata) {
        InitNode node = InitFactory.getInitNode(metadata.getRawType());
        if (node == null) {
            return List.of();
        }
        List<InitInstance> candidates = collectCandidates(node);
        List<InitInstance> confirmed = new ArrayList<>();
        for (InitInstance candidate : candidates) {
            TypeMetadata candidateType = candidate.getInitMetadata().getTypeMetadata();
            if (match(candidateType, metadata)) {
                confirmed.add(candidate);
            }
        }
        return confirmed;
    }

    public static List<InitInstance> collectCandidates(InitNode node) {
        List<InitInstance> result = new ArrayList<>();
        List<InitInstance> current = InitFactory.getTypeList(node.getClazz());
        if (current != null) {
            result.addAll(current);
        }
        for (InitNode child : node.getChildren()) {
            result.addAll(collectCandidates(child));
        }
        return result;
    }

}
