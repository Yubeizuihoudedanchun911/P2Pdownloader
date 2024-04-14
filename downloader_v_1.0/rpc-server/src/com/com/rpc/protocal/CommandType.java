package com.com.rpc.protocal;

public class CommandType {
    /**
     * cmd : type of command
     */
    public static final int REQ_DOWNLOAD = 2;
    public static final int HEART_BEAT = 3;
    public static final int REQ_VOTE = 4;
    public static final int VOTE = 5;
    public static final int INVOKE = 6;
    public static final int INVOKE_RESP = 7;
    public static final int DATA_TRANSFER = 8;
    public static final int COMMAND = 9;
    public static final int COMMAND_ACK = 10;
    public static final int DOWNLOAD_ACK = 11;

    public static final int REQ_JOIN_TO_TRACKER = 12;
    public static final int RESP_JOIN_TO_TRACKER = 13;


    public static final int NOTICE_GROUPNODES_ONLINE = 16;

}
