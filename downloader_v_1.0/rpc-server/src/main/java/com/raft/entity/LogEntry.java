package com.raft.entity;

public class LogEntry<T> {
    private long index;
    private long term;
    private T comand;

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

    public T getComand() {
        return comand;
    }

    public void setComand(T comand) {
        this.comand = comand;
    }

    public LogEntry(long index, long term, T comand) {
        this.index = index;
        this.term = term;
        this.comand = comand;
    }
}
