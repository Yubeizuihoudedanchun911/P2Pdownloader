package com.com.raft.state;


import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson2.JSON;
import com.com.raft.common.RaftNode;
import com.com.rpc.protocal.CommandType;
import com.com.rpc.protocal.Request;


public class Candidate implements State {

    private static final Logger log = LoggerFactory.getLogger("Candidate");
    private RaftNode raftNode;
    private Thread electionThread;
    private Set<String> voteRecord;
    private Set<String> refuseRecord;
    private volatile boolean electionSuccess;
    //    private int electionLimit;
    private boolean run;

    public Candidate(RaftNode raftNode) {
        this.raftNode = raftNode;
        voteRecord = new ConcurrentSkipListSet<>();
        refuseRecord = new ConcurrentSkipListSet<>();
        this.voteRecord.add(raftNode.getMe().getHost() + ":" + raftNode.getMe().getPort());
        //        electionLimit = raftNode.getNodeSet().size()/2;
        electionSuccess = false;
        electionThread = new Thread(() -> {
            run = true;
            while (run && !electionSuccess) {
                raftNode.election();
                try {
                    long reelection_time = 1500L;
                    Thread.sleep(reelection_time);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (voteRecord.size() < refuseRecord.size() && !electionSuccess) {
                    log.info("CandidateToFollower by timeLimit");
                    toFolower();
                } else {
                    log.info("CandidateToleader");
                    electionSuccess = true;
                    toLeader();
                }
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
        Boolean flag = JSON.parseObject(request.getObj().toString(), Boolean.class);
        synchronized (voteRecord) {
            String src = request.getSrcNode().getHost() + ":" + request.getSrcNode().getPort();
            if (!voteRecord.contains(src)) {
                if (flag) {
                    voteRecord.add(src);
                    log.info("get vote from " + src);
                } else {
                    refuseRecord.add(src);
                    log.info("get vote from " + src);
                }
            }

        }
    }

    @Override
    public String currentState() {
        return "candidate";
    }

    @Override
    public void dealMessage(Request request) {
        switch (request.getCmd()) {
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


    private void toFolower() {
        run = false;
        raftNode.CandidateToFollower();
    }

    private void toLeader() {
        run = false;
        raftNode.CandidateToLeader();
    }
}
