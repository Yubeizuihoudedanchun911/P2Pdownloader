package com.raft.common;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson2.JSON;
import com.raft.entity.LogEntry;
import com.raft.entity.VoteEntity;
import com.rpc.protocal.Request;


public class StateMachine {
    public long index = 0;

    public long term = 0;

    private final Map<Long, LogEntry> localDB = new HashMap<>();


    //  检测Leader发送的logEntry 是否与当前的 log匹配
    public boolean match(Request<LogEntry> req) {
        return req.getObj().getIndex() == index + 1 && req.getObj().getTerm() == term + 1;
    }

    public long getIndex() {
        return index;
    }

    public void setIndex(long index) {
        this.index = index;
    }

    public long getTerm() {
        return term;
    }

    public void setTerm(long term) {
        this.term = term;
    }

    public Map<Long, LogEntry> getLocalDB() {
        return localDB;
    }

    public boolean logCompare(Request request) {
        VoteEntity voteEntity = JSON.parseObject(request.getObj().toString(), VoteEntity.class);
        return term <= voteEntity.getLastLogTerm() || index < voteEntity.getLastLogIndex();
    }

}
