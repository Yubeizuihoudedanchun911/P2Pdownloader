package com.com.raft.state;

import com.alibaba.fastjson2.JSON;
import com.com.raft.common.Node;
import com.com.raft.entity.Command;
import com.com.raft.entity.HeapPoint;
import com.com.raft.util.HeartBeatTask;
import com.com.raft.common.RaftNode;
import com.com.rpc.protocal.CommandType;
import com.com.rpc.protocal.Request;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * raftNode state type
 */

public class Leader implements State {

    private static final Logger log = LoggerFactory.getLogger("Leader");
    private HeartBeatTask heartBeatTask;
    private RaftNode raftNode;

    public Leader(RaftNode raftNode) {
        heartBeatTask = new HeartBeatTask(raftNode);
        heartBeatTask.start();
        raftNode.setLeader(raftNode.getMe());
        this.raftNode = raftNode;
    }

    @Override
    public String currentState() {
        return "Leader";
    }

    @Override
    public void dealMessage(Request request) {
        int cmd = request.getCmd();

        switch (cmd) {
            case CommandType.REQ_DOWNLOAD:
                log.info("receive download req ");
//                handleDownloadRequst(request);
                break;
            case CommandType.HEART_BEAT:
                heartBeatRely(request);
                break;
            case CommandType.COMMAND_ACK:
                hanldeCommandACK(request);
                break;
        }
    }

    private void hanldeCommandACK(Request request) {
        Integer doneTask = JSON.parseObject(request.getObj().toString(), Integer.class);
        Node node = request.getSrcNode();
        HeapPoint heapPoint = raftNode.getTaskMap().get(node);
        heapPoint.setTaskLeft(heapPoint.getTaskLeft() - doneTask);
        raftNode.getTaskMap().put(node, heapPoint);
        log.info("after received handle command ack task map " + raftNode.getTaskMap().toString());
    }

//    private void handleDownloadRequst(Request request) {
//        Command command = JSON.parseObject(request.getObj().toString(), Command.class);
//        String uri = command.getTargetUri();
//        Node srcNode = request.getSrcNode();
//        DownLoadReuestEntry downLoadReuestEntry =JSON.parseObject(command.getObj().toString(),DownLoadReuestEntry.class);
//        DownloadCenter downloadCenter = raftNode.getDownloadCenter();
//        SlicePageInfo slicePageInfo = downloadCenter.getSilcePageInfo(uri);
//        Request req;
//        if(downLoadReuestEntry.isFullDownload()) {
//            ackToSender(uri, srcNode, slicePageInfo.getPages());
//            req = downloadCenter.downloadArrange(uri, srcNode, slicePageInfo);
//        }else {
//            SlicePageInfo finalSlicePageInfo = new SlicePageInfo();
//            CopyOnWriteArrayList<SliceInfo> sliceInfos = new CopyOnWriteArrayList<>();
//            List<Integer> list = downLoadReuestEntry.getSlicesIndex();
//           for(Integer i : list){
//               for( SliceInfo sliceInfo : slicePageInfo.getSliceInfoList()){
//                   if(sliceInfo.getPage()==i){
//                       sliceInfos.add(sliceInfo);
//                   }
//               }
//           }
//            finalSlicePageInfo.setSliceInfoList(sliceInfos);
//            req = downloadCenter.downloadArrange(uri,srcNode,finalSlicePageInfo);
//        }
//        log.info("download arranged");
//        raftNode.broadcastToAll(req);
//    }

    private void ackToSender(String uri, Node sender, int toTalPages) {
        Command<Integer> command = new Command<>(uri, toTalPages, sender);
        Request<Command> request = new Request<>(CommandType.DOWNLOAD_ACK, raftNode.getMe(), command);
        log.info("send ack to download req sender");
        raftNode.send(sender, request);

    }

    private void heartBeatRely(Request request) {
        if (raftNode.getStateMachine().logCompare(request)) {
            log.info("CandidateToFollower by election");
            raftNode.setLeader(request.getSrcNode());
            toFollower();
        }
    }

    private void toFollower() {
        raftNode.leaderToFollower();
    }
}
