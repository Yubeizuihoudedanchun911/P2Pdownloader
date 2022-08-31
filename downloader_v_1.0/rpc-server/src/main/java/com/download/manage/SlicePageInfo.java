package com.download.manage;

import lombok.Data;

import java.util.concurrent.CopyOnWriteArrayList;

@Data
public class SlicePageInfo {
    private CopyOnWriteArrayList<SliceInfo> sliceInfoList;

    private Long pages;
}
