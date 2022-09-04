package com.codec;

import com.alibaba.fastjson2.JSON;
import com.protocal.Request;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class RequstDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        String json = byteBuf.toString(StandardCharsets.UTF_8);
        Request request = JSON.parseObject(json, Request.class);
        list.add(request);
    }
}
