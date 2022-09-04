package com;

import com.raft.common.Node;
import com.raft.common.RaftNode;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Main2 {
    public static void main(String[] args) {
        RaftNode s1 = new RaftNode("localhost", 7070, "s2");
        s1.start();
    }
}
