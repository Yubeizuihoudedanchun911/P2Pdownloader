package com.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class FileEncoder extends MessageToByteEncoder<File>{
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, File file, ByteBuf byteBuf) throws Exception {
        FileChannel fileChannel = new  FileInputStream(file).getChannel();
        ByteBuffer buffer = ByteBuffer.allocateDirect(2<<20);
        int byteNum = fileChannel.read(buffer);
        while(byteNum!=-1){
            buffer.flip();
            byteBuf.writeByte(buffer.get(buffer.capacity()));
            buffer.clear();
            byteNum = fileChannel.read(buffer);
        }

        fileChannel.close();
    }
}
