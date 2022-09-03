package com.raft.state;

import com.alibaba.fastjson2.JSON;
import com.api.DownloadCenter;
import com.api.download.manage.SlicePageInfo;
import com.raft.common.Node;
import com.raft.entity.LogEntry;
import com.raft.util.HeartBeatTask;
import com.raft.common.RaftNode;
import com.rpc.protocal.CommandType;
import com.rpc.protocal.Request;
import lombok.extern.slf4j.Slf4j;

/**
 * raftNode state type
 */
@Slf4j
public class Leader implements State{
    private HeartBeatTask heartBeatTask ;
    private RaftNode raftNode;

    public Leader(RaftNode raftNode) {
        heartBeatTask = new HeartBeatTask(raftNode);
        heartBeatTask.start();
        this.raftNode = raftNode;
    }

    @Override
    public String currentState() {
        return "Leader";
    }

    @Override
    public void dealMessage(Request request) {
        int cmd = request.getCmd();

        switch (cmd){
            case CommandType.REQ_DOWNLOAD:
                log.info("receive download req " );
                handleDownloadRequst(request);
            case CommandType.HEART_BEAT:
                heartBeatRely(request);
        }
    }

    private  void handleDownloadRequst(Request request){
        String uri = request.getObj().toString();
        Node srcNode = request.getSrcNode();
        DownloadCenter downloadCenter = raftNode.getDownloadCenter();
        SlicePageInfo slicePageInfo = downloadCenter.getSilcePageInfo(uri);
        ackToSender(srcNode,slicePageInfo.getPages());
        Request req = downloadCenter.downloadArrange(uri,srcNode,slicePageInfo);
        log.info("download arranged");
        raftNode.broadcastToAll(req);
    }

    private  void ackToSender(Node sender ,long toTalPages){
        Request<Long> request = new Request<>(CommandType.DOWNLOAD_ACK,raftNode.getMe(),toTalPages);
        log.info("send ack to download req sender");
        raftNode.send(sender,request);
    }

    private  void heartBeatRely(Request request){
        LogEntry logEntry = JSON.parseObject(request.getObj().toString(),LogEntry.class);
        if (raftNode.getStateMachine().logCompare(request)) {
            log.info("CandidateToFollower by election");
            raftNode.setLeader(request.getSrcNode());
            toFollower();
        }
    }

    private void toFollower(){
        raftNode.leaderToFollower();
    }
}
