package com.common;

import com.Server;
import com.ServerController;
import com.protocal.Request;

public class RequestProcessor {
    public static ServerController server;

    public static void handleRequest(Request request){
        server.handleRequst(request);
    }
}
