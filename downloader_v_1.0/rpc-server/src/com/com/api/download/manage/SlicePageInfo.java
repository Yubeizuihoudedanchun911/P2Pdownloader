package com.com.api.download.manage;

import java.util.concurrent.CopyOnWriteArrayList;


public class SlicePageInfo {
    private CopyOnWriteArrayList<SliceInfo> sliceInfoList;

    private int pages;

    public CopyOnWriteArrayList<SliceInfo> getSliceInfoList() {
        return sliceInfoList;
    }

    public void setSliceInfoList(CopyOnWriteArrayList<SliceInfo> sliceInfoList) {
        this.sliceInfoList = sliceInfoList;
    }

    public int getPages() {
        return pages;
    }

    public void setPages(int pages) {
        this.pages = pages;
    }
}
