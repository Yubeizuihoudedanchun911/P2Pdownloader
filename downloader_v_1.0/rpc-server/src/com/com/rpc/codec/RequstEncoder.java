package com.com.rpc.codec;

import java.nio.charset.StandardCharsets;

import com.google.gson.Gson;
import com.com.rpc.protocal.Request;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class RequstEncoder extends MessageToByteEncoder<Request> {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Request request, ByteBuf byteBuf)
            throws Exception {
        Gson gson = new Gson();
        String json = gson.toJson(request);
        byteBuf.writeBytes(Unpooled.copiedBuffer(json.getBytes(StandardCharsets.UTF_8)));
    }
}
