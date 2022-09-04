package com.protocal;

public class CommandType {
    /**
     * cmd : type of command
     */
    public static final int RPC_INVOKE = 1;
    public static final int REQ_DOWNLOAD = 2;
    public static final int HEART_BEAT = 3;
    public static final int REQ_VOTE = 4;
    public static final int VOTE = 5;
    public static final int STATUS = 6;
    public static final int ACK = 7;
    public static final int DATA_TRANSFER = 8;
    public static final int COMMAND = 9;
    public static final int REQ_ACK = 10;
    public static final int DOWNLOAD_ACK = 11;

    public static final int REQ_JOIN_TO_TRACKER = 12;
    public static final int RESP_JOIN_TO_TRACKER =13;
    public static final int LEADER_UPDATE_GROUP = 14;
    public static final int RESP_TRACKER_TO_LEADER_ACK=15;


    public static final int REQ_NOTICE_GROUP_NODE = 16;

}
