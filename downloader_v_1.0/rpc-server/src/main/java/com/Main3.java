package com;

import com.raft.common.Node;
import com.raft.common.RaftNode;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Main3 {
    public static void main(String[] args) {
        Map<String, Node> map = new ConcurrentHashMap<>();
        Node n1 = new Node("localhost",7070);
        Node n2 = new Node("localhost",8080);
        Node n3 = new Node("localhost",6060);
        map.put("s1",n1);
        map.put("s2",n2);
        map.put("s3",n3);
        RaftNode s1 = new RaftNode("localhost", 6060, "s3");
        s1.setNodeMap(map);
        s1.start();
    }
}
