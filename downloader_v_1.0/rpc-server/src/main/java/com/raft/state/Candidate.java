package com.raft.state;


import com.alibaba.fastjson2.JSON;
import com.google.gson.reflect.TypeToken;
import com.raft.common.Node;
import com.raft.common.RaftNode;
import com.raft.entity.LogEntry;
import com.raft.entity.VoteEntity;
import com.rpc.protocal.CommandType;
import com.rpc.protocal.Request;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Type;
import java.net.Socket;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class Candidate implements State {
    private RaftNode raftNode;
    private Thread electionThread;
    private Set<String> voteRecord;
    private volatile boolean electionSuccess;
    private int electionLimit;
    private  boolean run ;

    public Candidate(RaftNode raftNode) {
        this.raftNode = raftNode;
        voteRecord = new HashSet<>();
        this.voteRecord.add(raftNode.getMe().getHost()+":"+raftNode.getMe().getPort());
        electionLimit = raftNode.getNodeMap().size()/2;
        electionSuccess = false;
        electionThread = new Thread(() -> {
            run = true;
            while (run && !electionSuccess && voteRecord.size() <= electionLimit) {
                raftNode.election();
                try {
                    long reelection_time = 1500L;
                    Thread.sleep(reelection_time);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (voteRecord.size() <= electionLimit && !electionSuccess) {
                log.info("CandidateToFollower by timeLimit");
                toFolower();
            }else{
                log.info("CandidateToleader");
                toLeader();
            }
        });
        electionThread.start();
    }

    private void electionReply(Request request) {
        if (raftNode.getStateMachine().logCompare(request)) {
            log.info("CandidateToFollower by election");
            raftNode.setLeader(request.getSrcNode());
            toFolower();
        } else {
            raftNode.vote(request.getSrcNode(), false);
        }
    }

    private void getVote(Request request) {
        Boolean flag = JSON.parseObject(request.getObj().toString(),Boolean.class);
        synchronized (voteRecord){
            String src = request.getSrcNode().getHost()+":"+request.getSrcNode().getPort();
            if(!voteRecord.contains(src) && flag){
                voteRecord.add(src);
                log.info("get vote from " + src);
            }
            if(voteRecord.size()> electionLimit){
                electionSuccess = true;
                log.info("election success ");
                raftNode.setLeader(raftNode.getMe());
                toLeader();
            }
        }
    }

    @Override
    public String currentState() {
        return "candidate";
    }

    @Override
    public void dealMessage(Request request) {
        switch (request.getCmd()){
            case CommandType.HEART_BEAT:
                toFolower();
                break;
            case CommandType.VOTE:
                getVote(request);
                break;
            case CommandType.REQ_VOTE:
                electionReply(request);
                break;

        }

    }


    private void toFolower(){
        run = false;
        raftNode.CandidateToFollower();
    }

    private void toLeader(){
        run = false;
        raftNode.CandidateToLeader();
    }
}
