package com;

public class ServerStarter {
    public static void main(String[] args) {
        ServerController serverController = new ServerController(912,10);
        serverController.run();
    }
}
