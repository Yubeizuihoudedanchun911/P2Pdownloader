package com;

import com.raft.common.Node;
import com.raft.common.RaftNode;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Main3 {
    public static void main(String[] args) {
        RaftNode s1 = new RaftNode("localhost", 6060, "s3");
        s1.start();
    }
}
