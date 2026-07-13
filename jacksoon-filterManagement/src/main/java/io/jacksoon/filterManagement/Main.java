package io.jacksoon.filterManagement;

import io.jacksoon.init.factory.InitFactory;

public class Main {
    public static void main(String[] args) {
        InitFactory.initialize("io.jacksoon.filterManagement");
        InitFactory.get(FilterManagementApplication.class).start();
    }
}
