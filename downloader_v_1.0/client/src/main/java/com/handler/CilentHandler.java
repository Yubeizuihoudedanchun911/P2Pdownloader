package com.handler;

import com.alibaba.fastjson.JSON;
import com.protoc.Invocation;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
public class CilentHandler extends ChannelInboundHandlerAdapter implements Callable {
    private static final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private ChannelHandlerContext ctx;
    private Invocation invocation;
    private  Object res;


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
        String invok  = JSON.toJSONString(invocation);
        ctx.writeAndFlush(Unpooled.copiedBuffer(invok.getBytes(StandardCharsets.UTF_8)));
        System.out.println("download requset is sent");
        wait();


        return res;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String name = UUID.randomUUID().toString();
        File file = new File("E://Java/tmep",name);
        FileChannel fileChannel = new FileOutputStream(file).getChannel();
        List<ByteBuf> byteBufs = (List<ByteBuf>)msg;
        executorService.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                try {
                    for(ByteBuf byteBuf : byteBufs){
                        fileChannel.write(byteBuf.nioBuffer());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
                res = file;
                System.out.println("下载成功！下载地址："+file.getAbsolutePath());
                return true;
            }
        });
    }

}
