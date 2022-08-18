package com;

import com.api.DownLoadService;
import com.api.impl.DownLoadServiceimpl;
import com.codec.FileDecoder;
import com.codec.FileEncoder;
import com.factory.BeanFactory;
import com.handler.NettyServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.util.HashMap;
import java.util.Map;

public class NettyServer {
    private int port;
    private static BeanFactory beanFactory;

    public NettyServer(int port) {
        this.port = port;
        init();
    }

    public void init(){
        beanFactory = new BeanFactory();
        beanFactory.regist(DownLoadService.class.getName(), DownLoadServiceimpl.class);
    }

    public void run(){
        EventLoopGroup parent = new NioEventLoopGroup(1);
        EventLoopGroup children = new NioEventLoopGroup();

        try{
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(parent,children)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG,128)
                    .childOption(ChannelOption.SO_KEEPALIVE,true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline()
//                                    .addLast(new HttpServerCodec())
//                                    .addLast(new FileDecoder())
                                    .addLast(new FileEncoder())
                                    .addLast(new NettyServerHandler())
                                    .addLast("encode", new ObjectEncoder())
                                    .addLast("decode", new ObjectDecoder(ClassResolvers.cacheDisabled(this
                                            .getClass().getClassLoader())));
//                                    .addLast("decoder", new StringDecoder())
//                            //向pipeline加入编码器
//                                    .addLast("encoder", new StringEncoder());
                        }
                    });

            ChannelFuture cf = bootstrap.bind(port).sync();
            cf.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            parent.shutdownGracefully();
            children.shutdownGracefully();
        }
    }
}
