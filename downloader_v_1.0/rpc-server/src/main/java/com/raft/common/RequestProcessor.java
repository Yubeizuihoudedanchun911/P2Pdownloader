package com.raft.common;

import com.rpc.protocal.CommandType;
import com.rpc.protocal.Request;

public class RequestProcessor {
    public static RaftNode raftNode;

    public static RaftNode getRaftNode() {
        return raftNode;
    }

    public static void setRaftNode(RaftNode raftNode) {
        RequestProcessor.raftNode = raftNode;
    }

    public static void handleRequst(Request req){
        raftNode.dealMessage(req);
    }
}
