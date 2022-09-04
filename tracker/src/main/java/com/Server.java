package com;

import com.codec.RequstDecoder;
import com.codec.RequstEncoder;
import com.handler.RequstHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class Server {
    private int port;
    private static ServerBootstrap bootstrap1;

    public Server(int port) {
        this.port = port;
        init();
    }

    public void init() {
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
        bootstrap1 = bootstrap;
    }

    public void run() {
        try {
            ChannelFuture cf = bootstrap1.bind(port).sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
