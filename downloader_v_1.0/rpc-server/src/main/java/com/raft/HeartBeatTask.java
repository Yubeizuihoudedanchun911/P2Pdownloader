package com.raft;

import com.raft.common.RaftNode;

public class HeartBeatTask {
    //  默认心跳间隔
    private  final int DEFUALT_HEARTBEAT_INTERVAL = 1000;
    private RaftNode raftNode;
    private  Thread heartBeat;

    public HeartBeatTask(RaftNode raftNode) {
        this.raftNode = raftNode;
    }

    public void start(){
        new Thread(()->{
            while(true){
                raftNode.heartBeat();
                try {
                    Thread.sleep(DEFUALT_HEARTBEAT_INTERVAL);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        heartBeat.start();
    }

    public boolean stop(){
        heartBeat.interrupt();
        return !heartBeat.isAlive();
    }
}
