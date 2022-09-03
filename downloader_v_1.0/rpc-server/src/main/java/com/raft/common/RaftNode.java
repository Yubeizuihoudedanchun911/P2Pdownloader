package com.raft.common;

import com.alibaba.fastjson.JSON;
import com.api.DownLoadCenterimpl;
import com.api.DownloadCenter;
import com.raft.entity.HeapPoint;
import com.raft.entity.LogEntry;
import com.raft.entity.VoteEntity;
import com.raft.state.Candidate;
import com.raft.state.Follower;
import com.raft.state.Leader;
import com.raft.state.State;
import com.rpc.client.RpcClient;
import com.rpc.protocal.CommandType;
import com.rpc.protocal.Request;
import com.rpc.server.RpcNettyServer;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Data
public class RaftNode {
    private String name;
    private State state;
    private Map<String, Node> nodeMap;
    private Node me;
    private StateMachine stateMachine;
    private RpcNettyServer server;
    private RpcClient client;
    private DownloadCenter downloadCenter;
    private  Node leader;
    private PriorityQueue<HeapPoint> priorityQueue;
    private  Thread downloadReqSender;
    private Object lock;

    public RaftNode(String host, int port, String name) {
        this.name = name;
        nodeMap = new ConcurrentHashMap<>();
        stateMachine = new StateMachine();
        me = new Node(host, port);
        Node n1 = new Node("localhost",7070);
        Node n2 = new Node("localhost",8080);
        Node n3 = new Node("localhost",6060);
        nodeMap.put("s1",n1);
        nodeMap.put("s2",n2);
        nodeMap.put("s3",n3);
        RequestProcessor.setRaftNode(this);
        init();
    }

    public void init_PriorityQueue() {
        priorityQueue = new PriorityQueue<HeapPoint>((a, b) -> {
            return a.getTaskLeft() - b.getTaskLeft();
        });
        for (Map.Entry<String, Node> entry : nodeMap.entrySet()) {
            HeapPoint heapPoint = new HeapPoint(entry.getValue(), 0);
            priorityQueue.add(heapPoint);
        }
    }

    public void init() {
        server = new RpcNettyServer(me.getPort());
        client = new RpcClient();
        init_PriorityQueue();
        state = new Follower(this);
        lock = new Object();
        downloadCenter = new DownLoadCenterimpl(this);
    }

    public void send(Node node, Request req) {
        ChannelFuture cf = client.connect(node.getHost(), node.getPort());
        Channel channel = cf.channel();
        channel.writeAndFlush(req);
    }

    public void start() {
        server.run();

        log.info("RAFTNODE RPCServer start...");
    }

    public void dealMessage(Request req) {
        int cmd = req.getCmd();
        if (cmd == CommandType.COMMAND) {
            Thread downloadThread = new Thread(() -> {
                log.info("node received download command");
                downloadCenter.dealDownload(req, this);
            });
            downloadThread.start();
        } else if (cmd == CommandType.DATA_TRANSFER) {
            log.info("received data_transfer ");
            downloadCenter.receiveSlice(req);
            if (downloadCenter.getPages() == 0) {
                downloadCenter.downLoadLoacalCombine();
            }
        }else if(cmd == CommandType.DOWNLOAD_ACK){
            long pages = JSON.parseObject(req.getObj().toString(),Long.class);
            downloadCenter.setPages(pages);
        }
        else state.dealMessage(req);
    }

    /**
     * send request to each peer(RaftNode) according to nodeMap
     */

    public void broadcast(Request req) {
        for (Map.Entry<String, Node> entry : nodeMap.entrySet()) {
            Node node = entry.getValue();
            Thread thread = new Thread(() -> {
                if (!node.equals(me)) {
                    send(node, req);
                }
            });
            thread.start();

        }

    }

    public void broadcastToAll(Request req) {
        for (Map.Entry<String, Node> entry : nodeMap.entrySet()) {
            Node node = entry.getValue();
            Thread thread = new Thread(() -> {
                send(node, req);
            });
            thread.start();

        }

    }

    /**
     * RaftNode Send HeartBeat Singal
     */
    public void heartBeat() {
        Request req = new Request(CommandType.HEART_BEAT, me, new LogEntry(stateMachine.getIndex(),stateMachine.getTerm(),null));
        broadcast(req);
    }

    ;

    /**
     * starts election
     */
    public void election() {

        VoteEntity entry = new VoteEntity(stateMachine.getIndex(), stateMachine.getTerm());
        stateMachine.term++;
        Request req = new Request(CommandType.REQ_VOTE, me, entry);
        broadcast(req);
    }


    public synchronized void followerToCandidate() {
        if (state.currentState().equals("follower")) {
            state = new Candidate(this);
            log.info("followerToCandidate");
        }
    }
    public synchronized void leaderToFollower() {
        if (state.currentState().equals("leader")) {
            state = new Follower(this);
            log.info("leaderToFollower");
        }
    }

    public synchronized void CandidateToLeader() {
        if (state.currentState().equals("candidate")) {
            state = new Leader(this);
            log.info("CandidateToLeader");
        }
    }

    public synchronized void CandidateToFollower() {
        if (state.currentState().equals("candidate")) {
            state = new Follower(this);
            log.info("candidateToFollower");
        }
    }

    public void vote(Node node, Boolean boll) {
        send(node, new Request<Boolean>(CommandType.VOTE, me, boll));
    }

    public void download(String uri, String fileName) {
        Request req = downloadCenter.downloadReq(uri, fileName);
         downloadReqSender = new Thread(()->{
            try {
                synchronized (lock) {
                    lock.wait();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.info("download req sended ");
            send(leader, req);
        });
        downloadReqSender.start();
    }

    public  void setLeader(Node node){
        synchronized (lock) {
            leader = node;
            lock.notify();
        }
    }
    public  Node  getLeader(){
        return leader;
    }


}
