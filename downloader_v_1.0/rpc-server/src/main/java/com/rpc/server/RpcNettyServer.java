package com.rpc.server;

import com.download.DownLoadService;
import com.download.impl.DownLoadServiceimpl;
import com.rpc.codec.FileEncoder;
import com.rpc.factory.BeanFactory;
import com.rpc.handler.NettyServerHandler;
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
        beanFactory.regist(DownLoadService.class.getName(), DownLoadServiceimpl.class);
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
//                                    .addLast(new HttpServerCodec())
//                                    .addLast(new FileDecoder())
                                .addLast(new FileEncoder())
                                .addLast(new NettyServerHandler());
//                                    .addLast("encode", new ObjectEncoder())
//                                    .addLast("decode", new ObjectDecoder(ClassResolvers.cacheDisabled(this
//                                            .getClass().getClassLoader())));
//                                    .addLast("decoder", new StringDecoder())
//                            //向pipeline加入编码器
//                                    .addLast("encoder", new StringEncoder());
                    }
                });
    }

    public void run() {
        try {
            ChannelFuture cf = bootstrap.bind(port).sync();
            cf.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
