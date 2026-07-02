package io.jacksoon.registry;

import io.jacksoon.init.factory.InitFactory;

public class Main {
    public static void main(String[] args) {
        InitFactory.initialize("io.jacksoon.registry");
        RegistryApplication application = InitFactory.get(RegistryApplication.class);
        application.start();

    }
}