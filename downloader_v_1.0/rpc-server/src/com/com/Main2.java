package com.com;

import com.com.raft.common.RaftNode;

public class Main2 {
    public static void main(String[] args) {
        RaftNode s1 = new RaftNode("localhost", 7070, "s2");
        s1.start();
    }
}
