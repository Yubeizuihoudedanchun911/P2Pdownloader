package com.rpc.factory;

import com.alibaba.fastjson.JSON;
import com.rpc.protocal.Invocation;
import com.rpc.protocal.Request;
import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
@Slf4j
public class RequstParser {
    public static  Invocation parse(Object msg ) {
        ByteBuf byteBuf = (ByteBuf) msg;
        log.info("json : " + byteBuf.toString(StandardCharsets.UTF_8));
        Invocation invocation = JSON.parseObject(byteBuf.toString(StandardCharsets.UTF_8),Invocation.class);
        return invocation;
    }

//    public static <T>  Request<T> parseReq(Object msg ){
//        ByteBuf byteBuf = (ByteBuf) msg;
//        Request requst = JSON.parseObject(byteBuf.toString(StandardCharsets.UTF_8),Request.class);
//        int cmd = requst.getCmd();
//        switch (cmd):
//
//    }


}
