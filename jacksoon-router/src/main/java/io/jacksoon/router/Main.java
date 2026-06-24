package io.jacksoon.router;

import io.jacksoon.init.factory.InitFactory;

public class Main {
    public static void main(String[] args) {
        InitFactory.initialize("io.jacksoon.router");
        RouterApplication application = InitFactory.get(RouterApplication.class);
        application.start();
    }
}