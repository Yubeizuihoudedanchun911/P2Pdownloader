package com;

import com.raft.common.RaftNode;

public class Main {
    public static void main(String[] args) {
        RaftNode s1 = new RaftNode("localhost", 8080, "s1");
        s1.start();
        s1.download(
                "https://www.douyin.com/download/pc/obj/douyin-pc-client/7044145585217083655/releases/8969298/1.3"
                        + ".0/win32-ia32/douyin-v1.3.0-win32-ia32-douyinDownload1.exe",
                "douyin");
    }
}
    