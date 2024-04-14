package com.com.rpc.client;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.com.rpc.codec.RequstDecoder;
import com.com.rpc.codec.RequstEncoder;
import com.com.rpc.handler.RequstHandler;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.FixedRecvByteBufAllocator;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;


public class RpcClient {
    private static Bootstrap bs;


    private static final Logger log = LoggerFactory.getLogger("RpcClient");

    public static Bootstrap getBs() {
        return bs;
    }

    public static void setBs(Bootstrap bs) {
        RpcClient.bs = bs;
    }

    public RpcClient() {
        initClient();
    }


    //初始化客户端
    private static void initClient() {
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

    public ChannelFuture connect(String addrss, int port) {
        try {
            //            log.info("connect to " + addrss + ": " + port);
            ChannelFuture ccf = bs.connect(addrss, port).sync();
            return ccf;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}
