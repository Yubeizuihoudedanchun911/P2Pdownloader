package com.rpc.codec;
import com.rpc.protocal.MessageProtocol;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;

@Slf4j
public class FileEncoder extends MessageToByteEncoder<File>{
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, File file, ByteBuf byteBuf) throws Exception {
        log.info("called");
        FileChannel fileChannel = new  FileInputStream(file).getChannel();
        ByteBuffer buffer = ByteBuffer.allocateDirect(1<<20);
        int byteNum = fileChannel.read(buffer);
        while(byteNum!=-1){
            buffer.flip();
//            ByteBuf res = Unpooled.copiedBuffer(buffer);
//            byteBuf.writeBytes(res,0,res.capacity());
            byte[] bs = new byte[byteNum];
            buffer.get(bs);
            MessageProtocol messageProtocol = new MessageProtocol();
            messageProtocol.setLen(byteNum);
            messageProtocol.setContent(bs);

            byteBuf.writeInt(messageProtocol.getLen());
            byteBuf.writeBytes(messageProtocol.getContent());
            log.info("position : "  + messageProtocol.getLen());
            System.out.println(Arrays.toString(messageProtocol.getContent()));
            buffer.clear();
            byteNum = fileChannel.read(buffer);
        }
        log.info("sent");
        fileChannel.close();
    }
}
