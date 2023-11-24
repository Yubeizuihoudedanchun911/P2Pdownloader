package com.rpc.handler;

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.raft.common.RequestProcessor;
import com.rpc.protocal.CommandType;
import com.rpc.protocal.Request;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;


public class RequstHandler extends SimpleChannelInboundHandler<Request> implements Callable {

    private static final Logger log = LoggerFactory.getLogger("RequstHandler");
    private Object res;
    private Request rpcCall;
    private ChannelHandlerContext ctx;

    public Object getRes() {
        return res;
    }

    public void setRes(Object res) {
        this.res = res;
    }

    public Request getRpcCall() {
        return rpcCall;
    }

    public void setRpcCall(Request rpcCall) {
        this.rpcCall = rpcCall;
    }

    public ChannelHandlerContext getCtx() {
        return ctx;
    }

    public void setCtx(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Request request) throws Exception {
        if (request.getCmd() == CommandType.INVOKE) {
            Request resp = RequestProcessor.handleInvoke(request);
            channelHandlerContext.writeAndFlush(resp);
        } else if (request.getCmd() == CommandType.INVOKE_RESP) {
            synchronized (this) {
                res = request.getObj();
                notify();
            }
        } else {
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
