package com;

import com.alibaba.fastjson2.JSON;
import com.common.Group;
import com.common.Node;
import com.common.RequestProcessor;
import com.entity.GroupEntry;
import com.protocal.CommandType;
import com.protocal.Request;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArraySet;

@Slf4j
public class ServerController {
    private int port;
    private Server server;
    private Client client;
    private volatile PriorityQueue<Group> balance_stress;
    private static int GROUP_LIMIT;
    private Node me;
    private volatile Map<Node, Group> registNodesMap;

    public ServerController(int port, int limit) {
        this.port = port;
        GROUP_LIMIT = limit;
        init();
    }

    private void init() {
        server = new Server(port);
        client = new Client();
        balance_stress = new PriorityQueue<>();
        registNodesMap = new ConcurrentHashMap<>();
        RequestProcessor.server = this;
        try {
            me = new Node(InetAddress.getLocalHost().getHostAddress(), port);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        server.run();
    }

    public void handleRequst(Request request) {
        int cmd = request.getCmd();
        log.info("receive req from " + request.getSrcNode().getHost() + ":" + request.getSrcNode().getPort() + "cmd : " + cmd);
        switch (cmd) {
            case CommandType
                    .REQ_JOIN_TO_TRACKER:
                handleJoinReq(request);
            log.info(""+registNodesMap);
            break;
            case CommandType.LEADER_UPDATE_GROUP:
                handleUpdate(request);
                break;
        }
    }

    private void handleJoinReq(Request request) {
        synchronized (this) {
            Group group = balance_stress.peek();
            Node srcNode = request.getSrcNode();
            Set<Node> nodes;
            if (group == null || (group.size() > GROUP_LIMIT && !registNodesMap.containsKey(srcNode))) {
                nodes = new CopyOnWriteArraySet<>();
                group = new Group(nodes);
                balance_stress.add(group);
                registNodesMap.put(srcNode, group);
            } else {
                if (registNodesMap.containsKey(srcNode)) {
                    group = registNodesMap.get(srcNode);
                }
                nodes = group.getOnline_nodes();
            }
            GroupEntry groupEntry = new GroupEntry(nodes, group.getGroupID());
            Request<GroupEntry> req = new Request<>(CommandType.RESP_JOIN_TO_TRACKER, me, groupEntry);

            send(srcNode, req);
            group.joinGroup(srcNode);
            log.info(registNodesMap.toString());
            log.info("join req success ");
        }
    }

    private void handleUpdate(Request req) {
        synchronized (this) {
            GroupEntry groupUpdateEntry = JSON.parseObject(req.getObj().toString(), GroupEntry.class);
            Node srcNode = req.getSrcNode();
            Set<Node> list = groupUpdateEntry.getNodes();
            Group group = registNodesMap.get(srcNode);
            group.leftGroup(list);
            Request<Boolean> request = new Request<>(CommandType.RESP_TRACKER_TO_LEADER_ACK, me, true);
            send(srcNode, request);
        }
    }

    private void send(Node targetNode, Request req) {
        ChannelFuture connect = client.connect(targetNode.getHost(), targetNode.getPort());
        Channel channel = connect.channel();
        channel.writeAndFlush(req);
        channel.closeFuture();
    }
}

