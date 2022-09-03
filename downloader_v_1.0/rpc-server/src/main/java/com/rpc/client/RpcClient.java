package com.rpc.client;


import com.rpc.codec.FileDecoder;
import com.rpc.codec.RequstDecoder;
import com.rpc.codec.RequstEncoder;
import com.rpc.handler.CilentHandler;
import com.rpc.handler.RequstHandler;
import com.rpc.protocal.Invocation;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Proxy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Data
@Slf4j
public class RpcClient {

    //创建线程池
    private static ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private static CilentHandler client;
    private  static Bootstrap bs;

    public RpcClient() {
        initClient();
    }

    //编写方法使用代理模式，获取一个代理对象

    public Object getProxy(final Class<?> serivceClass) {

        return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                new Class<?>[]{serivceClass}, (proxy, method, args) -> {

                    //{}  部分的代码，客户端每调用一次 hello, 就会进入到该代码
                    if (client == null) {
                        initClient();
                    }
                    String[] argTye = new String[args.length];
                    for (int i = 0; i < args.length; i++) {
                        argTye[i] = args[i].getClass().getName();
                    }
                    Invocation invocation = new Invocation(serivceClass.getName(),method.getName(),argTye,args);
                    client.setInvocation(invocation);
                    return executor.submit(client).get();

                });

    }

    //初始化客户端
    private static void initClient() {
        client = new CilentHandler();
        //创建EventLoopGroup
        NioEventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
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
                                        .addLast(new RequstHandler());
                            }
                        }
                );
        bs = bootstrap;

    }

    public ChannelFuture connect(String addrss , int port){
        try {
//            log.info("connect to " + addrss + ": " + port);
            ChannelFuture ccf  = bs.connect(addrss, port).sync();
            return ccf;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
