package com.raft.util;

import com.raft.common.RaftNode;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HeartBeatTask {
    //  默认心跳间隔
    private  final int DEFUALT_HEARTBEAT_INTERVAL =  5000;
    private RaftNode raftNode;
    private  Thread heartBeat;

    public HeartBeatTask(RaftNode raftNode) {
        this.raftNode = raftNode;
    }

    public void start(){
        heartBeat = new Thread(()->{
            while(true){
                log.info(System.currentTimeMillis()+" ");
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
