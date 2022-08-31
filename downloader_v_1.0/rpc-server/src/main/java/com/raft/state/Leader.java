package com.raft.state;

import com.raft.HeartBeatTask;
import com.raft.common.RaftNode;
import com.rpc.protocal.Request;

/**
 * raftNode state type
 */
public class Leader implements State{
    private HeartBeatTask heartBeatTask ;

    public Leader(RaftNode raftNode) {
        heartBeatTask = new HeartBeatTask(raftNode);
        heartBeatTask.start();
    }

    @Override
    public String currentState() {
        return "Leader";
    }

    @Override
    public void dealMessage(Request request) {

    }
}
