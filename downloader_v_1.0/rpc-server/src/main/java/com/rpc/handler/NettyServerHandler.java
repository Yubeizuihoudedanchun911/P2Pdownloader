//package com.rpc.handler;
//
//
//import com.rpc.protocal.Invocation;
//
//import io.netty.channel.ChannelHandlerContext;
//import io.netty.channel.ChannelInboundHandlerAdapter;
//import io.netty.util.concurrent.DefaultEventExecutorGroup;
//import io.netty.util.concurrent.EventExecutorGroup;
//import lombok.extern.slf4j.Slf4j;
//
//
//import java.io.*;
//import java.lang.reflect.InvocationTargetException;
//import java.lang.reflect.Method;
//
//
//@Slf4j
//public class NettyServerHandler extends ChannelInboundHandlerAdapter {
//    static final EventExecutorGroup group = new DefaultEventExecutorGroup(16);
//    static  final String tempPath = "E://Java";
//
//
//    @Override
//    public void channelRead(ChannelHandlerContext ctx, Object msg) throws InstantiationException, IllegalAccessException, NoSuchMethodException, IOException, ClassNotFoundException {
//        System.out.println("read");
//
//        Invocation req = RequstParser.parse(msg);;
//        Object[] args = req.args;
//        String[] stringType = req.argType;
//        Class<?>[] argType = new Class[stringType.length];
//        ClassLoader loader = ClassLoader.getSystemClassLoader();
//        for (int i = 0; i < stringType.length; i++) {
//            argType[i] = loader.loadClass(stringType[i]);
//        }
//
//        String method = req.method;
//        String className = req.className;
//        Object implClass = BeanFactory.getObj(className);
//        Method invokeMethod = implClass.getClass().getMethod(method, argType);
//
//        try {
//            File res = (File) invokeMethod.invoke(implClass,args);
//            log.info(res.getAbsolutePath());
//            ctx.writeAndFlush(res);
//            log.info("writed");
//        } catch (InvocationTargetException e) {
//            e.printStackTrace();
//        }
//
//
//    }
//
//    @Override
//    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
//        cause.printStackTrace();
//    }
//
//
//    @Override
//    public void channelActive(ChannelHandlerContext ctx) throws Exception {
//        System.out.println("connet");
//    }
//}
