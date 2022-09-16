package com.rpc.handler;

import com.rpc.protocal.CommandType;
import com.rpc.protocal.Invocation;
import com.rpc.protocal.Request;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import  com.raft.common.RequestProcessor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;

@Slf4j
@Data
public class RequstHandler extends SimpleChannelInboundHandler<Request> implements Callable{
    private Object res;
    private Request rpcCall;
    private ChannelHandlerContext ctx;
    @Override
    protected  void channelRead0(ChannelHandlerContext channelHandlerContext, Request request) throws Exception {
        if(request.getCmd()== CommandType.INVOKE){
            Request resp = RequestProcessor.handleInvoke(request);
            channelHandlerContext.writeAndFlush(resp);
        } else if(request.getCmd()== CommandType.INVOKE_RESP){
            synchronized (this) {
                res = request.getObj();
                notify();
            }
        } else{
            RequestProcessor.handleRequst(request);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.info("handler occured a exception  ");
    }


    @Override
    public synchronized Object call() throws Exception {
        ctx.writeAndFlush(rpcCall);
        wait();
        return res;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
       this.ctx = ctx;
    }
}
