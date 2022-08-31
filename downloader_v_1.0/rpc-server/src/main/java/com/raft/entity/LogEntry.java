package com.raft.entity;

import lombok.Data;

@Data
public class LogEntry {
    private long index ;
    private long term ;
    private  Command comand;
    public LogEntry(long index, long term, Command comand) {
        this.index = index;
        this.term = term;
        this.comand = comand;
    }
}
