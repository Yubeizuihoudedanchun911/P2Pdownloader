package com;

public class Bootstrap {
    public static void main(String[] args) {
        NettyServer server = new NettyServer(7000);
        server.run();
    }
}
