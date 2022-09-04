package com.api;

import com.alibaba.fastjson2.JSON;
import com.api.download.manage.DownLoadUtil;
import com.api.download.manage.SliceInfo;
import com.api.download.manage.SlicePageInfo;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.raft.common.Node;
import com.raft.common.RaftNode;
import com.raft.entity.Command;
import com.raft.entity.HeapPoint;
import com.raft.entity.LogEntry;
import com.rpc.protocal.CommandType;
import com.rpc.protocal.MessageProtocol;
import com.rpc.protocal.Request;
import lombok.Data;
import lombok.Synchronized;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.*;
import java.util.concurrent.*;

import static com.api.download.manage.DownLoadUtil.*;

@Slf4j
@Data
public class DownLoadCenterimpl implements DownloadCenter {
    private static final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private RaftNode me;

    private String file_name;
    private volatile long ack_pages;
    private volatile long total_pages;

    public DownLoadCenterimpl(RaftNode me) {
        this.me = me;
    }

    public long getAck_pages() {
        return ack_pages;
    }

    public void setAck_pages(long ack_pages) {

        this.ack_pages = ack_pages;
        total_pages = ack_pages;
    }

    @Override
    public Request downloadReq(String uri, String fileName) {
        Request<String> req = new Request<>(2, me.getMe(), uri);
        this.file_name = fileName;
        return req;
    }

    @Override
    public void downLoadLoacalCombine() {
        mergeFileTranTo(tempPath, file_name, total_pages);
    }


    /**
     * 负载均衡： 使用小根堆 进行挑选当前任务较少的节点
     * to arrange downloadWork to each node
     *
     * @param downloadUrl
     */
    @Override
    public Request downloadArrange(String downloadUrl, Node targetNode,SlicePageInfo slicePageInfo) {
        Map<Node, HeapPoint> taskMap = me.getTaskMap();
        Collection<HeapPoint> values = taskMap.values();
        PriorityQueue<HeapPoint> heapPoints  = new PriorityQueue<HeapPoint>((a, b) -> {
            return a.getTaskLeft() - b.getTaskLeft();
        });
        heapPoints.addAll(values);

        Map<String, List<SliceInfo>> taskArrangement = new ConcurrentHashMap<>();
        for (SliceInfo sliceInfo : slicePageInfo.getSliceInfoList()) {
            HeapPoint h = heapPoints.poll();
            Node node = h.getNode();
            String key = node.getHost()+":"+node.getPort();
            List<SliceInfo> list = taskArrangement.getOrDefault(key, new CopyOnWriteArrayList<>());
            list.add(sliceInfo);

            taskArrangement.put(key, list);
            log.info(taskArrangement.toString());
            h.setTaskLeft(h.getTaskLeft() + 1);
            taskMap.put(node,h);
            heapPoints.add(h);
        }

        Command<Map<Node, List<SliceInfo>>> command = new Command(downloadUrl, taskArrangement, targetNode);
        LogEntry<Command> logEntry = new LogEntry(me.getStateMachine().getIndex(), me.getStateMachine().getTerm(), command);
        Request<LogEntry> req = new Request<>(CommandType.COMMAND, me.getMe(), logEntry);
        log.info("taskArrangement is sent");
        return req;
    }

    @Override
    public void receiveSlice(Request req) {
        MessageProtocol msg = JSON.parseObject(req.getObj().toString(), MessageProtocol.class);

        File file = new File(tempPath, msg.getSlice_idx() + "-" + file_name);
        if (file.exists() && file.length() == SLICE_SIZE) {
            log.info("此分片文件 {} 已存在", msg.getSlice_idx());
        }
        try {
            FileChannel fileChannel = new FileOutputStream(file).getChannel();
            byte[] content = msg.getContent();
            if (content != null && content.length == 0) {
                log.warn("分片文件：{},没有内容", file.getName());
            }
            fileChannel.write(ByteBuffer.wrap(content));
            fileChannel.close();
            log.info("数据写入成功");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        synchronized(this) {
            this.ack_pages -= 1;
        }

    }

    public void dealDownload(Request request, RaftNode curNode) {
        LogEntry logEntry = JSON.parseObject(request.getObj().toString(),LogEntry.class);
        Command command =JSON.parseObject(logEntry.getComand().toString(),Command.class);
        Node tarNode = command.getTargetNode();
        String downloadUrl = command.getTargetUri();
        Map<String, List<SliceInfo>> taskArrangement = JSON.parseObject(command.getTaskArrangement().toString(), new TypeToken<Map<String,List<SliceInfo>>>(){}.getType());
        String key = me.getMe().getHost()+":"+me.getMe().getPort();
        List<SliceInfo> list = taskArrangement.get(key);
        if(list == null ) return ; // todo

        for (SliceInfo sliceInfo : list) {
            executorService.submit(() -> {
                File tmpFile = DownLoadUtil.download(tempPath, downloadUrl, sliceInfo, "" + tarNode.getHost() + "_" + tarNode.getPort() + "-" + UUID.randomUUID());
                try {
                    FileChannel fileChannel = new FileInputStream(tmpFile).getChannel();
                    ByteBuffer buffer = ByteBuffer.allocateDirect(1 << 20);
                    int byteNum = fileChannel.read(buffer);
                    buffer.flip();
                    byte[] content = new byte[byteNum];
                    buffer.get(content);
                    MessageProtocol messageProtocol = new MessageProtocol();
                    messageProtocol.setLen(byteNum);
                    messageProtocol.setContent(content);
                    messageProtocol.setSlice_idx(sliceInfo.getPage());
                    Request<MessageProtocol> req = new Request<>(CommandType.DATA_TRANSFER, request.getSrcNode(), messageProtocol);
                    curNode.send(tarNode, req);
                    log.info("node download task finished " + downloadUrl + ": " + sliceInfo.getPage());
                    fileChannel.close();
                    tmpFile.delete();


                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
        sendCommandAck(request.getSrcNode(),list.size());


    }

    //  每个下载node 将下载的总片输 回交给leader 进行管理
    public void sendCommandAck(Node leader , int doneTask){
        Request req = new Request<Integer>(CommandType.COMMAND_ACK, me.getMe(),doneTask);
        me.send(leader,req);
    }

    @Override
    public SlicePageInfo getSilcePageInfo(String downloadUrl) {
        long size = getTotalSize(downloadUrl);
        SlicePageInfo slicePageInfo = splitPage(size);
        return slicePageInfo;
    }

    @Override
    public void setPages(long pages) {
        this.setAck_pages(pages);
    }

    @Override
    public long getPages() {
        return this.getAck_pages();
    }

}
