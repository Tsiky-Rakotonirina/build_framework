package itu.framework;

import itu.framework.core.Dispatcher;

public class Main {
    public static void main(String[] args) throws Exception {
        Dispatcher dispatcher = new Dispatcher("itu.framework.demo");

        System.out.println("Has /hello? " + dispatcher.hasRoute("/hello", "GET"));
        System.out.println("/hello -> " + dispatcher.dispatch("/hello", "GET"));

        System.out.println("Has /sum? " + dispatcher.hasRoute("/sum", "GET"));
        System.out.println("/sum -> " + dispatcher.dispatch("/sum", "GET"));
    }
}
