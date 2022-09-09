package com.raft.entity;

import lombok.Data;

@Data
public class LogEntry<T> {
    private long index ;
    private long term ;
    private T comand;
    public LogEntry(long index, long term, T comand) {
        this.index = index;
        this.term = term;
        this.comand = comand;
    }
}
