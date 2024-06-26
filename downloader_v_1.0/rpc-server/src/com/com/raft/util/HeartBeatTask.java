package com.com.raft.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.com.raft.common.RaftNode;

public class HeartBeatTask {
    private static Logger log = LoggerFactory.getLogger("HeartBeatTask");
    //  默认心跳间隔
    private final int DEFUALT_HEARTBEAT_INTERVAL = 5000;
    private RaftNode raftNode;
    private Thread heartBeat;

    public HeartBeatTask(RaftNode raftNode) {
        this.raftNode = raftNode;
    }

    public void start() {
        heartBeat = new Thread(() -> {
            while (true) {
                log.info(System.currentTimeMillis() + " ");
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

    public boolean stop() {
        heartBeat.interrupt();
        return !heartBeat.isAlive();
    }
}
