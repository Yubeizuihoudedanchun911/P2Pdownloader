package com.com.raft.entity;

import java.util.List;


public class DownLoadReuestEntry {
    private boolean fullDownload;
    private List<Integer> slicesIndex;

    public boolean isFullDownload() {
        return fullDownload;
    }

    public void setFullDownload(boolean fullDownload) {
        this.fullDownload = fullDownload;
    }

    public List<Integer> getSlicesIndex() {
        return slicesIndex;
    }

    public void setSlicesIndex(List<Integer> slicesIndex) {
        this.slicesIndex = slicesIndex;
    }

    public DownLoadReuestEntry(boolean fullDownload, List<Integer> slicesIndex) {
        this.fullDownload = fullDownload;
        this.slicesIndex = slicesIndex;
    }
}
