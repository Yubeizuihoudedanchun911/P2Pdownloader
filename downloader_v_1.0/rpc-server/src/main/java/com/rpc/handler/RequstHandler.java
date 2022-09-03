package com.rpc.handler;

import com.rpc.protocal.Request;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import  com.raft.common.RequestProcessor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;

@Slf4j
public class RequstHandler extends SimpleChannelInboundHandler<Request>{
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Request request) throws Exception {
        RequestProcessor.handleRequst(request);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.info("handler occured a exception  ");
    }

}
