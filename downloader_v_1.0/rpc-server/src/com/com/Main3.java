package com.com;

import com.com.raft.common.RaftNode;

public class Main3 {
    public static void main(String[] args) {
        RaftNode s1 = new RaftNode("localhost", 6060, "s3");
        s1.start();
    }
}
