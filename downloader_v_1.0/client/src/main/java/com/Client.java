package com;

import com.codec.FileDecoder;
import com.codec.FileEncoder;
import com.handler.CilentHandler;
import com.protoc.Invocation;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import lombok.Data;

import java.lang.reflect.Proxy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Data
public class Client {

    //创建线程池
    private static ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private static int port;
    private static CilentHandler client;
    private  static ChannelFuture cf;

    public Client(int port) {
        this.port = port;
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
                                pipeline.addLast(new FileDecoder())
                                        .addLast(client);
                            }
                        }
                );

        try {
            bootstrap.connect("127.0.0.1", port).sync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
