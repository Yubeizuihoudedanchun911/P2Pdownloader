//package com.raft.common;
//
//import com.alibaba.fastjson.JSON;
//import com.rpc.client.RpcClient;
//import com.rpc.protocal.Request;
//import com.rpc.server.RpcNettyServer;
//import io.netty.channel.Channel;
//import io.netty.channel.ChannelFuture;
//
//public class NetWork {
//    private  String hostname;
//    private  int port;
//    private Thread serverThread;
//    private boolean run ;
//    private  Object lock;
//
//
//
//    public NetWork(String hostname, int port) {
//        this.hostname = hostname;
//        this.port = port;
//
//    }
//
//    public void serverStart(){
//        serverThread = new Thread(()->{
//                server.run();
//        });
//        serverThread.start();
//    }

//    public Object getRPCSender(Node node,Class clazz){
//        client.connect(node.getHost(),node.getPort());
//        return client.getProxy(clazz);
//    }




//}
