package com.raft.state;

import com.alibaba.fastjson2.JSON;
import com.google.gson.reflect.TypeToken;
import com.raft.common.Node;
import com.raft.common.RaftNode;
import com.raft.entity.LogEntry;
import com.raft.entity.VoteEntity;
import com.raft.util.TimeUtil;
import com.rpc.protocal.CommandType;
import com.rpc.protocal.Request;
import jdk.nashorn.internal.ir.RuntimeNode;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Type;

/**
 * author : YBzhddc_911
 */
@Slf4j
@Data
public class Follower implements State {
    private RaftNode raftNode;
    private TimeUtil timeUtil;
    private Thread timeMonitor;
//    private Thread voteTimeCounter;
    private TimeUtil voteCD;
    private volatile boolean run;
    private final long checkTime = 10 * 1000;
    private Node votedFor;
//    private volatile boolean electionRun;

    public Follower(RaftNode raftNode) {
        this.raftNode = raftNode;
        timeUtil = new TimeUtil(checkTime);
        voteCD = new TimeUtil(15 * 1000L);
        timeMonitorStart();
    }


    /**
     * create a subThread to count time
     * over time limit : to start a election & state changed as candidate
     */

    private void timeMonitorStart() {
        run = true;
        timeMonitor = new Thread(() -> {
            while (run) {
                try {
                    Thread.sleep(checkTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                synchronized (timeUtil) {
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

    private void restTimeUtil() {
        log.info("rest count time");
        synchronized (timeUtil) {
            timeUtil.updateLastTime();
        }
        log.info("keep waiting...");
    }

    private void restVoteCD() {
        log.info("rest count time");
        synchronized (voteCD) {
            voteCD.updateLastTime();
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
        switch (cmd) {
            case CommandType.RPC_INVOKE: // not finished
                break;
            case CommandType.HEART_BEAT:
                handleHeartbeat(request);
                break;
            case CommandType.REQ_VOTE:
                electionVoteReply(request);
                break;
            default:
                log.error("request not matched ");

        }
    }

    /**
     * vote to candidate or not
     *
     * @param request server requst
     */
    private void electionVoteReply(Request request) {
        log.info("election vote");
        VoteEntity voteEntity = JSON.parseObject(request.getObj().toString(), VoteEntity.class);
        if ((votedFor == null || voteCD.isOvertime()) && voteStratagy(voteEntity)) {
            raftNode.vote(request.getSrcNode(), true);
            votedFor = request.getSrcNode();
            raftNode.getStateMachine().term = voteEntity.getLastLogTerm();
            restTimeUtil();
        } else {
            raftNode.vote(request.getSrcNode(), false);
        }
        restVoteCD();

    }

    private boolean voteStratagy(VoteEntity voteEntity) {
        if (voteEntity.getLastLogTerm() > raftNode.getStateMachine().getTerm()) {
            return true;
        } else if (voteEntity.getLastLogTerm() == raftNode.getStateMachine().getTerm()) {
            return voteEntity.getLastLogIndex() >= raftNode.getStateMachine().getIndex();
        } else {
            return false;
        }
    }

    private void handleHeartbeat(Request request){
        log.info("recevie heartbeat ");
        LogEntry logEntry = JSON.parseObject(request.getObj().toString(),LogEntry.class);
        raftNode.setLeader(request.getSrcNode());
        raftNode.getStateMachine().setTerm(logEntry.getTerm());
        restTimeUtil();
    }
}
