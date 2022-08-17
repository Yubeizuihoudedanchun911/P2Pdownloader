package com.handler;


import com.factory.BeanFactory;
import com.factory.GsonParser;
import com.protocal.Invocation;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import lombok.extern.slf4j.Slf4j;


import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


@Slf4j
public class NettyServerHandler extends ChannelInboundHandlerAdapter {
    static final EventExecutorGroup group = new DefaultEventExecutorGroup(16);
    static  final String tempPath = "E://Java";


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws InstantiationException, IllegalAccessException, NoSuchMethodException, IOException, ClassNotFoundException {
        System.out.println("read");

        Invocation req = GsonParser.parse(msg);;
        Object[] args = req.args;
        String[] stringType = req.argType;
        Class<?>[] argType = new Class[stringType.length];
        ClassLoader loader = ClassLoader.getSystemClassLoader();
        for (int i = 0; i < stringType.length; i++) {
            argType[i] = loader.loadClass(stringType[i]);
        }

        String method = req.method;
        String className = req.className;
        Class implClass = BeanFactory.getObj(className);
        Method invokeMethod = implClass.getMethod(method, argType);

        try {
            ctx.writeAndFlush(invokeMethod.invoke(implClass,args));
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("connet");
    }
}
