package com.handler;

import com.common.RequestProcessor;
import com.protocal.Request;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RequstHandler extends SimpleChannelInboundHandler<Request>{
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Request request) throws Exception {
        RequestProcessor.handleRequest(request);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.info("handler occured a exception  ");
    }

}
