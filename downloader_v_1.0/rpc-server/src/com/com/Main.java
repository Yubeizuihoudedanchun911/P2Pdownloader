package com.com;

import com.com.raft.common.RaftNode;

public class Main {
    public static void main(String[] args) {
        RaftNode s1 = new RaftNode("localhost", 8080, "s1");
        s1.start();
        s1.download(
                "https://www.douyin.com/download/pc/obj/douyin-pc-client/7044145585217083655/releases/11634728/3.5.1/darwin-universal/douyin-v3.5.1-darwin-universal.dmg","douyin");
    }
}
    