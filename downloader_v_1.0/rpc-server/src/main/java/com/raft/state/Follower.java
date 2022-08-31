package com.raft.state;

import com.raft.common.Node;
import com.raft.common.RaftNode;
import com.raft.util.TimeUtil;
import com.rpc.protocal.CommandType;
import com.rpc.protocal.Request;
import jdk.nashorn.internal.ir.RuntimeNode;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class Follower implements State {
    private RaftNode raftNode;
    private TimeUtil timeUtil;
    private Thread timeMonitor;
    private TimeUtil voteCD;
    private volatile boolean run;
    private final int checkTime = 200;
    private Node votedFor;

    public Follower(RaftNode raftNode){
        this.raftNode = raftNode;
        timeUtil = new TimeUtil();
        voteCD = new TimeUtil(1L);
        timeMonitorStart();
    }

    private void timeMonitorStart() {
        run = true;
        timeMonitor = new Thread(()->{
            while (run) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                synchronized (timeUtil){
                    if (timeUtil.isOvertime()) {
                        run = false;
                        raftNode.followerToCandidate();
                        break;
                    }
                }
            }
        });
        timeMonitor.start();
    }

    private void restTimeUtil(){
        log.info("rest count time");
        synchronized (timeUtil){
            timeUtil.updateLastTime();
        }
        log.info("keep waiting...");
    }

    @Override
    public String currentState() {
        return "follower";
    }

    @Override
    public void dealMessage(Request request) {
        int cmd = request.getCmd();
        switch (cmd){
            case CommandType.RPC_INVOKE: // not finished
                break;
            case CommandType.HEART_BEAT:
                restTimeUtil();
                break;
            case CommandType.VOTE:


        }
    }

    private void electionVoteReply(){
        log.info("election vote");

    }

    private  boolean voteStratagy(Request<>){

    }
}
