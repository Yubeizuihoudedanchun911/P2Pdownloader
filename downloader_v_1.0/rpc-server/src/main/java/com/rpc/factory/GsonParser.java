package com.rpc.factory;

import com.alibaba.fastjson.JSON;
import com.rpc.protocal.Invocation;
import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
@Slf4j
public class GsonParser {
    public static  Invocation parse(Object msg ) {
        ByteBuf byteBuf = (ByteBuf) msg;
//        JSONReader jsonReader = new JSONReader();
        log.info("json : " + byteBuf.toString(StandardCharsets.UTF_8));
//        return (byteBuf.toString(StandardCharsets.UTF_8));
        Invocation invocation = JSON.parseObject(byteBuf.toString(StandardCharsets.UTF_8),Invocation.class);
        return invocation;
    }


}
