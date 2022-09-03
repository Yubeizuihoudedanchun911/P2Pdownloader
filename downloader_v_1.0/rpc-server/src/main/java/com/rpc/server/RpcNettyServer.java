package com.rpc.server;

import com.rpc.codec.RequstDecoder;
import com.rpc.codec.RequstEncoder;
import com.rpc.factory.BeanFactory;
import com.rpc.handler.RequstHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class RpcNettyServer {
    private int port;
    private static BeanFactory beanFactory;
    private static ServerBootstrap bootstrap;

    public RpcNettyServer(int port) {
        this.port = port;
        init();
    }

    public void init() {
        beanFactory = new BeanFactory();
        EventLoopGroup parent = new NioEventLoopGroup(1);
        EventLoopGroup children = new NioEventLoopGroup();

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(parent, children)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 128)
                .option(ChannelOption.SO_BROADCAST, true)
                .option(ChannelOption.RCVBUF_ALLOCATOR, new FixedRecvByteBufAllocator(65535))
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline()
                                .addLast(new RequstDecoder())
                                .addLast(new RequstEncoder())
                                .addLast(new RequstHandler());
                    }
                });
        this.bootstrap = bootstrap;
    }

    public void run() {
        try {
            ChannelFuture cf = bootstrap.bind(port).sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
