package com.rpc.codec;

import com.protoc.MessageProtocol;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

import java.util.List;

public class FileDecoder extends ReplayingDecoder<Void>  {

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> list) throws Exception {
        System.out.println("receieved...");
        int len = in.readInt();
        MessageProtocol messageProtocol = new MessageProtocol();
        messageProtocol.setLen(len);
        byte[] content = new byte[len];
        in.readBytes(content);
        messageProtocol.setContent(content);
        list.add(messageProtocol);
    }
}
