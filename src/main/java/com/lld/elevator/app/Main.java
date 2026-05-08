package com.lld.elevator.app;

public class Main {
    public static void main(String[] args) throws Exception {
        ElevatorHttpServer server = new ElevatorHttpServer();
        server.start();
    }
}