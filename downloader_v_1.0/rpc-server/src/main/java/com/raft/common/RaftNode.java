package com.raft.common;

import com.raft.state.Candidate;
import com.raft.state.Follower;
import com.raft.state.Leader;
import com.raft.state.State;
import com.rpc.client.RpcClient;
import com.rpc.protocal.Request;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
@Slf4j
public class RaftNode {
    private NetWork netWork;
    private String name;
    private  State state;
    private Map<String, Node> nodeMap;
    private Node me;
    private StateMachine stateMachine ;

    public RaftNode(String host, int port, String name) {
        netWork = new NetWork(host,port);
        this.name = name;
        nodeMap = new ConcurrentHashMap<>();
        stateMachine = new StateMachine();
        me = new Node(host,port);
        nodeMap.put("1",me);
        RequestProcessor.setRaftNode(this);

    }

    public void start(){
        state = new Follower(this);
        netWork.serverStart();
        log.info("RAFTNODE RPCServer start...");
    }

    public  void dealMessage(Request req){
        state.dealMessage(req);
    }

    /**
     *     send request to each peer(RaftNode) according to nodeMap
      */

    public void broadcast(Request req ){
        for(Map.Entry<String,Node> entry : nodeMap.entrySet()){
            Node node = entry.getValue();
            Thread thread = new Thread(()->{
              if(!node.equals(me)) {
                  netWork.send(node,req);
              }
            });
            thread.start();;
        }

    }

    /**
     * RaftNode Send HeartBeat Singal
     */
    public void heartBeat(){
        Request req = new Request(3,null,null);
        broadcast(req);
    };

    /**
     * starts election
     */
    public void election(){
        Request req = new Request(4,me,null);
        broadcast(req);
    }


    public synchronized void followerToCandidate(){
        if(state.currentState().equals("follower")){
            state = new Candidate(this);
            log.info("followerToCandidate");
        }
    }

    public synchronized void CandidateToLeader() {
        if(state.currentState().equals("candidate")){
            state = new Leader(this);
            log.info("CandidateToLeader");
        }
    }

    public synchronized void CandidateToFollower() {
        if(state.currentState().equals("candidate")){
            state = new Follower(this);
            log.info("candidateToFollower");
        }
    }

}
