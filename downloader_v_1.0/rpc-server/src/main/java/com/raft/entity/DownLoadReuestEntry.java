package com.raft.entity;

import lombok.Data;

import java.util.List;

@Data
public class DownLoadReuestEntry {
    private boolean fullDownload;
    private List<Integer> slicesIndex;

    public DownLoadReuestEntry(boolean fullDownload, List<Integer> slicesIndex) {
        this.fullDownload = fullDownload;
        this.slicesIndex = slicesIndex;
    }
}
