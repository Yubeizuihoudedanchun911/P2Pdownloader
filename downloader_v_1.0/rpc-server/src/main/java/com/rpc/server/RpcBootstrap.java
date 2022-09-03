package com.rpc.server;

public class RpcBootstrap {
    public static void main(String[] args) {
        RpcNettyServer server = new RpcNettyServer(7000);
//        server.run();
    }
}
