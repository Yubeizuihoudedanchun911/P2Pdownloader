package com.api;


import com.api.download.manage.SlicePageInfo;
import com.raft.common.Node;
import com.raft.common.RaftNode;
import com.rpc.protocal.Request;

import static com.api.download.manage.DownLoadUtil.getTotalSize;
import static com.api.download.manage.DownLoadUtil.splitPage;

public interface DownloadCenter {
    static final String tempPath = "E://Java/downloadTest";

    public Request downloadReq(String uri, String fileName);

    public void downLoadLoacalCombine();

    public Request downloadArrange(String uri, Node targetNode, SlicePageInfo slicePageIn);

    public void receiveSlice(Request req);

    public void dealDownload(Request request, RaftNode curNode);

    public SlicePageInfo getSilcePageInfo(String downloadUrl);

    public void setPages(long pages);
    public long getPages();


}