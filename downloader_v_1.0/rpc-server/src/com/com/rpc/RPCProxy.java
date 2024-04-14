package com.com.rpc;

import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.com.raft.common.Node;
import com.com.raft.common.RaftNode;
import com.com.rpc.codec.RequstDecoder;
import com.com.rpc.codec.RequstEncoder;
import com.com.rpc.handler.RequstHandler;
import com.com.rpc.protocal.Invocation;
import com.com.rpc.protocal.Request;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.FixedRecvByteBufAllocator;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class RPCProxy {

    public static Map<String, Object> rpcMap;

    private static ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    public static Object getProxy(Class<?> serivceClass, Node node) {
        return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                new Class<?>[] {serivceClass}, (proxy, method, args) -> {
                    //{}  部分的代码，客户端每调用一次 hello, 就会进入到该代码
                    String[] argTye = new String[args.length];
                    for (int i = 0; i < args.length; i++) {
                        argTye[i] = args[i].getClass().getName();
                    }
                    Invocation invocation = new Invocation(serivceClass.getName(), method.getName(), argTye, args);
                    return RPCInvoke(invocation, node);
                });
    }

    public static Object RPCInvoke(Invocation invocation, Node node) throws ExecutionException, InterruptedException {
        NioEventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        RequstHandler requstHandler = new RequstHandler();
        Request<Invocation> request = new Request<>(6, null, invocation);
        requstHandler.setRpcCall(request);
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_BROADCAST, true)
                .option(ChannelOption.RCVBUF_ALLOCATOR, new FixedRecvByteBufAllocator(65535))
                .handler(
                        new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel ch) throws Exception {
                                ChannelPipeline pipeline = ch.pipeline();
                                pipeline.addLast(new RequstDecoder())
                                        .addLast(new RequstEncoder())
                                        .addLast(requstHandler);
                            }
                        }
                );
        try {
            bootstrap.connect(node.getHost(), node.getPort()).sync();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Object res = executor.submit(requstHandler).get();
        return res;
    }

    public static Object getBean(RaftNode raftNode, String className) {
        Object s = rpcMap.get(className);
        return s;
    }
}
