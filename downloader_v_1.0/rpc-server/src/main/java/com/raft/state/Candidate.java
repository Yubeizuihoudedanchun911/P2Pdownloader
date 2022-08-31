package com.raft.state;


import com.raft.common.RaftNode;
import com.rpc.protocal.Request;

import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Candidate implements State{
    private RaftNode raftNode;
    public Candidate(RaftNode raftNode) {
        this.raftNode = raftNode;
    }

    @Override
    public String currentState() {
        return "candidate";
    }

    @Override
    public void dealMessage(Request request) {

    }
}
