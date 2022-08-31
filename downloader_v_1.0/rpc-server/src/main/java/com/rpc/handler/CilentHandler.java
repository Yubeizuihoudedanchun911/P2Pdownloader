package com.rpc.handler;

import com.alibaba.fastjson.JSON;

import com.rpc.protocal.Invocation;
import com.rpc.protocal.MessageProtocol;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CilentHandler extends SimpleChannelInboundHandler<MessageProtocol> implements Callable {
    private static final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private ChannelHandlerContext ctx;
    private Invocation invocation;
    private Object res;


    public Invocation getInvocation() {
        return invocation;
    }

    public void setInvocation(Invocation invocation) {
        this.invocation = invocation;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println(" channelActive 被调用  ");
        this.ctx = ctx;
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }

    @Override
    public synchronized Object call() throws Exception {
        String invok = JSON.toJSONString(invocation);
        ctx.writeAndFlush(Unpooled.copiedBuffer(invok.getBytes(StandardCharsets.UTF_8)));
        System.out.println("download requset is sent");
        wait();


        return res;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, MessageProtocol ms) throws Exception {
        String name = UUID.randomUUID().toString() +".png";
        System.out.println("read");
        File file = new File("E://Java/tmep", name);
        FileChannel fileChannel = new FileOutputStream(file).getChannel();
        executorService.submit(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                try {
                    fileChannel.write(ByteBuffer.wrap(ms.getContent()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;

            }

        });

        System.out.println("success");
    }

}
