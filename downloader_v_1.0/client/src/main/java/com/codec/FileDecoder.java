package com.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

import java.util.List;

public class FileDecoder extends ReplayingDecoder<Void> {

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
//        String name = UUID.randomUUID().toString();
//        File file = new File("E://Java/tmep",name);
//        FileChannel fileChannel = new FileOutputStream(file).getChannel();
//        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(2<<20);
        while(byteBuf.readableBytes()!=0){
            list.add(byteBuf.readBytes(2<<20));
        }
    }
}
