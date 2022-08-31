package com.raft.common;

import com.raft.entity.LogEntry;
import com.rpc.protocal.Request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class StateMachine {
    private final long index = 0;

    private final long term = 0;

    private final Map<Long, ArrayList<LogEntry>> localDB = new HashMap<>();


    //  检测Leader发送的logEntry 是否与当前的 log匹配
    public boolean match(Request<LogEntry> req){
        return req.getObj().getIndex()==index+1 && req.getObj().getTerm() == term+1;
    }

}
