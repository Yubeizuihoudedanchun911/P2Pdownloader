package com.raft.common;

import com.alibaba.fastjson.JSON;
import com.api.DownLoadCenterimpl;
import com.api.DownloadCenter;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.raft.entity.HeapPoint;
import com.raft.entity.LogEntry;
import com.raft.entity.VoteEntity;
import com.raft.state.Candidate;
import com.raft.state.Follower;
import com.raft.state.Leader;
import com.raft.state.State;
import com.rpc.client.RpcClient;
import com.rpc.protocal.CommandType;
import com.rpc.protocal.GroupEntry;
import com.rpc.protocal.Request;
import com.rpc.server.RpcNettyServer;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFuture;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.ConnectException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.rpc.RPCProxy.getProxy;
import static com.rpc.RPCProxy.rpcMap;

@Slf4j
@Data
public class RaftNode {
    private String name;
    private State state;
    private Set<Node> nodeSet;
    private Node me;
    private StateMachine stateMachine;
    private RpcNettyServer server;
    private RpcClient client;
    private DownloadCenter downloadCenter;
    private Node leader;
    private Map<Node,HeapPoint> taskMap;
    private Thread downloadReqSender;
    private Thread initConfigSender;
    private Object lock; // download sender lock
    private Object init_lock; // ger init config lock
    private final Node tracker = new Node("localhost", 911);
    private String groupID;
    private List<Node> disConnectNodes;

    public RaftNode(String host, int port, String name) {
        this.name = name;
        stateMachine = new StateMachine();
        me = new Node(host, port);

        RequestProcessor.setRaftNode(this);
        init();
    }

    @Override
    public String toString() {
        return "RaftNode{}";
    }

    public void init_Config() {
        initConfigSender = new Thread(()->{
            Request req = new Request(CommandType.REQ_JOIN_TO_TRACKER, me, null);
            send(tracker, req);
            synchronized (init_lock) {
                try {
                    init_lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                taskMap = new ConcurrentHashMap<>();
                for (Node node : nodeSet) {
                    HeapPoint heapPoint = new HeapPoint(node, 0);
                    taskMap.put(node,heapPoint);
                }
                Request request = new Request(CommandType.NOTICE_GROUPNODES_ONLINE,me,null);
                nodeOlineMsgSend();
                state = new Follower(this);

            }
        });

        initConfigSender.start();

    }

    private void nodeOlineMsgSend(){
        Request request = new Request(CommandType.NOTICE_GROUPNODES_ONLINE,me,null);
        broadcast(request);
    }

    public void init() {
        disConnectNodes = new CopyOnWriteArrayList<>();
        server = new RpcNettyServer(me.getPort());
        client = new RpcClient();
        lock = new Object();
        init_lock = new Object();
        downloadCenter = new DownLoadCenterimpl(this);
        rpcMap = new ConcurrentHashMap<>();
        rpcMap.put(DownloadCenter.class.getName(),downloadCenter);
    }

    public void send(Node node, Request req) {
        ChannelFuture cf = client.connect(node.getHost(), node.getPort());
        Channel channel = cf.channel();
        try {
            channel.writeAndFlush(req);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void start() {
        server.run();
        init_Config();
        log.info("RAFTNODE RPCServer start...");
    }

    public void dealMessage(Request req) {
        int cmd = req.getCmd();
        switch (cmd) {
            case CommandType.COMMAND:
                Thread downloadThread = new Thread(() -> {
                    log.info("node received download command");
//                    downloadCenter.dealDownload(req, this);
                    this.stateMachine.index++;
                });
                downloadThread.start();
                break;

            case  CommandType.DATA_TRANSFER:
                downloadCenter.dealDownloadTask(req);
                break;
            case  CommandType.DOWNLOAD_ACK:
                downloadCenter.dealDownloadTask(req);
            case  CommandType.RESP_JOIN_TO_TRACKER:
                get_config(req);
                break;
            case  CommandType.NOTICE_GROUPNODES_ONLINE:
                dealNodeOline(req);
                break;
            default: state.dealMessage(req);
            break;
        }
    }

    /**
     * send request to each peer(RaftNode) according to nodeSet
     */

    public void broadcast(Request req)  {
        for (Node node : nodeSet) {
            Thread thread = new Thread(() -> {
                if (!node.equals(me)) {
                        send(node, req);
                }
            });
            thread.start();

        }

    }

    public void get_config(Request request) {
        synchronized (init_lock) {

            GroupEntry configEntry = JSON.parseObject(request.getObj().toString(), GroupEntry.class);
            nodeSet = configEntry.getNodes();
            nodeSet.add(me);
            groupID = configEntry.getGroupID();
            init_lock.notify();
        }
        log.info("node online " + nodeSet);
    }

    public void broadcastToAll(LogEntry logEntry) {
        for (Node node : nodeSet) {
            Thread thread = new Thread(() -> {
                DownloadCenter proxy = (DownloadCenter)getProxy(DownloadCenter.class, node);
                int doneTask = proxy.dealDownload(logEntry);
                HeapPoint heapPoint = taskMap.get(node);
                heapPoint.setTaskLeft(heapPoint.getTaskLeft() - doneTask);
                taskMap.put(node,heapPoint);
                log.info(taskMap.toString());
            });
            thread.start();

        }

    }

    /**
     * RaftNode Send HeartBeat Singal
     */
    public void heartBeat() {
        Request req = new Request(CommandType.HEART_BEAT, me, new LogEntry(stateMachine.getIndex(), stateMachine.getTerm(), null));

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
        downloadReqSender = new Thread(() -> {
            try {
                synchronized (lock) {
                    lock.wait();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.info("download req sended ");
            downloadCenter.downloadReq(uri, fileName);
        });
        downloadReqSender.start();
    }

    public void setLeader(Node node) {
        synchronized (lock) {
            leader = node;
            lock.notify();
        }
    }

    public Node getLeader() {
        return leader;
    }

    public void dealNodeOline(Request request){
        Node srcNode = request.getSrcNode();
        log.info("recevie online note : " + srcNode.getHost() + ": " +srcNode.getPort());
        nodeSet.add(srcNode);
        taskMap.put(srcNode,new HeapPoint(srcNode,0));
    }


}
