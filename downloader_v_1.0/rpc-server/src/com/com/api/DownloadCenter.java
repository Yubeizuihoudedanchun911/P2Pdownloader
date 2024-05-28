package com.com.api;


import java.util.concurrent.CopyOnWriteArrayList;

import com.com.api.download.manage.SlicePageInfo;
import com.com.raft.common.Node;
import com.com.raft.entity.LogEntry;
import com.com.rpc.protocal.Request;

public interface DownloadCenter {
    public static final String tempPath = "/Users/ybzhddc_911/Desktop/test";

    public void downloadReq(String uri, String fileName);

    public LogEntry downloadArrange(String uri, Node targetNode, SlicePageInfo slicePageIn);

    public int dealDownload(LogEntry logEntry);

    public SlicePageInfo getSilcePageInfo(String downloadUrl);

    public void dealDownloadTask(Request request);

    public int fullDownLoad(String uri, Node me);

    public void partDownLoad(String uri, Node tarNode, CopyOnWriteArrayList<Integer> slices);


}
