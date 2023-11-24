package com.raft.entity;

public class VoteEntity {

    /**
     * 候选人的最后日志条目的索引值
     */
    long lastLogIndex;

    /**
     * 候选人最后日志条目的任期号
     */
    long lastLogTerm;

    public long getLastLogIndex() {
        return lastLogIndex;
    }

    public void setLastLogIndex(long lastLogIndex) {
        this.lastLogIndex = lastLogIndex;
    }

    public long getLastLogTerm() {
        return lastLogTerm;
    }

    public void setLastLogTerm(long lastLogTerm) {
        this.lastLogTerm = lastLogTerm;
    }

    public VoteEntity() {
    }

    public VoteEntity(long lastLogIndex, long lastLogTerm) {
        this.lastLogIndex = lastLogIndex;
        this.lastLogTerm = lastLogTerm;
    }
}
