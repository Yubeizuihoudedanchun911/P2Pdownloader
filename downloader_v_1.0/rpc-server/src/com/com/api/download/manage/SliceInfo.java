package com.com.api.download.manage;

/**
 * 每片的 信息
 */
public class SliceInfo {

    public long getSt() {
        return st;
    }

    public void setSt(long st) {
        this.st = st;
    }

    public long getEd() {
        return ed;
    }

    public void setEd(long ed) {
        this.ed = ed;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public SliceInfo(long st, long ed, int page) {
        this.st = st;
        this.ed = ed;
        this.page = page;
    }

    private long st;
    private long ed;
    private int page;
}
