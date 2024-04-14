package com.com.raft.common;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.com.rpc.RPCProxy;
import com.com.rpc.protocal.CommandType;
import com.com.rpc.protocal.Invocation;
import com.com.rpc.protocal.Request;

public class RequestProcessor {
    public static RaftNode raftNode;

    public static RaftNode getRaftNode() {
        return raftNode;
    }

    public static void setRaftNode(RaftNode raftNode) {
        RequestProcessor.raftNode = raftNode;
    }

    public static void handleRequst(Request req) {
        raftNode.dealMessage(req);
    }

    public static Request handleInvoke(Request request) {
        Invocation invocation = JSON.parseObject(request.getObj().toString(), Invocation.class);
        String className = invocation.getClassName();
        String method = invocation.getMethod();
        Object[] argsObj = invocation.getArgs();
        String[] argTypes = invocation.getArgType();
        Class<?>[] args = new Class[argTypes.length];
        ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
        Object bean = RPCProxy.getBean(raftNode, className);
        Object res = null;
        for (int i = 0; i < argTypes.length; i++) {
            try {
                args[i] = systemClassLoader.loadClass(argTypes[i]);
                if (argsObj[i].getClass() == com.alibaba.fastjson2.JSONObject.class
                        || argsObj[i].getClass() == JSONArray.class) {
                    argsObj[i] = JSON.parseObject(argsObj[i].toString(), args[i]);
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        try {
            Method doMethod = bean.getClass().getMethod(method, args);
            res = doMethod.invoke(bean, argsObj);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        Request<Object> resp = new Request<>(CommandType.INVOKE_RESP, raftNode.getMe(), res);
        return resp;
    }
}
