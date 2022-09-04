package com.api;

import com.alibaba.fastjson2.JSON;
import com.api.download.manage.DownLoadUtil;
import com.api.download.manage.SliceInfo;
import com.api.download.manage.SlicePageInfo;

import com.google.gson.reflect.TypeToken;
import com.raft.common.Node;
import com.raft.common.RaftNode;
import com.raft.entity.Command;
import com.raft.entity.DownLoadReuestEntry;
import com.raft.entity.HeapPoint;
import com.raft.entity.LogEntry;
import com.raft.util.TimeUtil;
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
    public Map<String, TaskListener> taskListenerMap;

    public DownLoadCenterimpl(RaftNode me) {
        this.me = me;
        taskListenerMap = new ConcurrentHashMap<>();
    }

    @Override
    public Request downloadReq(String uri, String fileName) {
        Command<DownLoadReuestEntry> command = new Command<>(uri, new DownLoadReuestEntry(true, null), me.getMe());
        Request<Command> req = new Request<Command>(2, me.getMe(), command);
        taskListenerMap.put(uri, new TaskListener(fileName, uri));
        return req;
    }

    /**
     * 负载均衡： 使用小根堆 进行挑选当前任务较少的节点
     * to arrange downloadWork to each node
     *
     * @param downloadUrl
     */
    @Override
    public Request downloadArrange(String downloadUrl, Node targetNode, SlicePageInfo slicePageInfo) {
        Map<Node, HeapPoint> taskMap = me.getTaskMap();
        Collection<HeapPoint> values = taskMap.values();
        PriorityQueue<HeapPoint> heapPoints = new PriorityQueue<HeapPoint>((a, b) -> {
            return a.getTaskLeft() - b.getTaskLeft();
        });
        heapPoints.addAll(values);

        Map<String, List<SliceInfo>> taskArrangement = new ConcurrentHashMap<>();
        for (SliceInfo sliceInfo : slicePageInfo.getSliceInfoList()) {
            HeapPoint h = heapPoints.poll();
            Node node = h.getNode();
            String key = node.getHost() + ":" + node.getPort();
            List<SliceInfo> list = taskArrangement.getOrDefault(key, new CopyOnWriteArrayList<>());
            list.add(sliceInfo);

            taskArrangement.put(key, list);
            h.setTaskLeft(h.getTaskLeft() + 1);
            taskMap.put(node, h);
            heapPoints.add(h);
        }

        Command<Map<Node, List<SliceInfo>>> command = new Command(downloadUrl, taskArrangement, targetNode);
        LogEntry<Command> logEntry = new LogEntry(me.getStateMachine().getIndex(), me.getStateMachine().getTerm(), command);
        Request<LogEntry> req = new Request<>(CommandType.COMMAND, me.getMe(), logEntry);
        log.info("taskArrangement is sent");
        log.info("after arranged task map :"+ me.getTaskMap().toString());
        return req;
    }

    @Override


    public void dealDownload(Request request, RaftNode curNode) {
        LogEntry logEntry = JSON.parseObject(request.getObj().toString(), LogEntry.class);
        Command command = JSON.parseObject(logEntry.getComand().toString(), Command.class);
        Node tarNode = command.getTargetNode();
        String downloadUrl = command.getTargetUri();
        Map<String, List<SliceInfo>> taskArrangement = JSON.parseObject(command.getObj().toString(), new TypeToken<Map<String, List<SliceInfo>>>() {
        }.getType());
        String key = me.getMe().getHost() + ":" + me.getMe().getPort();
        List<SliceInfo> list = taskArrangement.get(key);
        if (list == null) return;

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
                    Command<MessageProtocol> command1 = new Command<>(downloadUrl, messageProtocol, tarNode);
                    Request<Command> req = new Request<>(CommandType.DATA_TRANSFER, request.getSrcNode(), command1);
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
        sendCommandAck(request.getSrcNode(), list.size());


    }

    //  每个下载node 将下载的总片输 回交给leader 进行管理
    public void sendCommandAck(Node leader, int doneTask) {
        Request req = new Request<Integer>(CommandType.COMMAND_ACK, me.getMe(), doneTask);
        me.send(leader, req);
    }

    @Override
    public SlicePageInfo getSilcePageInfo(String downloadUrl) {
        long size = getTotalSize(downloadUrl);
        SlicePageInfo slicePageInfo = splitPage(size);
        return slicePageInfo;
    }

    @Override
    public void dealDownloadTask(Request request) {
        Command command = JSON.parseObject(request.getObj().toString(), Command.class);
        String key = command.getTargetUri();
        TaskListener taskListener = taskListenerMap.get(key);
        int cmd = request.getCmd();
        switch (cmd) {
            case CommandType.DATA_TRANSFER:
                log.info("received data_transfer ");
                taskListener.receiveSlice(command);
                if(taskListener.success){
                    taskListenerMap.remove(key);
                }
                break;
            case CommandType.DOWNLOAD_ACK:
                int pages = com.alibaba.fastjson.JSON.parseObject(command.getObj().toString(), Integer.class);
                taskListener.setTotalPages(pages);

                break;
        }
    }

    private class TaskListener {
        private volatile int totalPages;
        private volatile int leftPages;
        private volatile boolean[] recordPages;
        private String file_name;
        private boolean success;
        private TimeUtil timeUtil;
        private final long checkTime = 1000 * 10L;
        private String uri;

        public TaskListener(String file_name, String uri) {
            this.file_name = file_name;
            success = false;
            timeUtil = new TimeUtil(checkTime);
            this.uri = uri;
            count();
        }

        private void count() {
            executorService.execute(() -> {
                while (!isSuccess()) {
                    try {
                        Thread.sleep(checkTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (timeUtil.isOvertime()) {
                        log.info("go on download ... ");
                        log.info("left pages ... " + leftPages);
                        goOnDownload();
                    } else {
                        restTimeUtil();
                    }
                }
            });
        }

        private void restTimeUtil() {
            log.info("download rest count time");
            synchronized (timeUtil) {
                timeUtil.updateLastTime();
            }
        }

        public int getTotalPages() {
            return totalPages;
        }

        public void setTotalPages(int totalPages) {
            this.totalPages = totalPages;
            this.leftPages = totalPages;
            recordPages = new boolean[totalPages];
        }


        public boolean isSuccess() {
            return success;
        }


        public void receiveSlice(Command command) {

            MessageProtocol msg = JSON.parseObject(command.getObj().toString(), MessageProtocol.class);

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
                this.recordPages[msg.getSlice_idx() - 1] = true;
                synchronized (this) {
                    leftPages -= 1;
                }
                if (0 == leftPages && !isSuccess()) {
                    downLoadLoacalCombine();
                }
                restTimeUtil();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void downLoadLoacalCombine() {
            log.info("combine called ");
            synchronized (this) {
                mergeFileTranTo(tempPath, file_name, totalPages);
                success = true;
            }
        }

        private List<Integer> checkSlice() {
            List<Integer> slicesIndex = new CopyOnWriteArrayList<>();
            for (int i = 0; i < totalPages; i++) {
                if (!recordPages[i]) {
                    slicesIndex.add(i);
                }
            }
            return slicesIndex;
        }

        private void goOnDownload() {
            List<Integer> slicesIndex = checkSlice();
            Command<DownLoadReuestEntry> command = new Command<>(uri, new DownLoadReuestEntry(false, slicesIndex), me.getMe());
            Request<Command> req = new Request<Command>(2, me.getMe(), command);
            me.send(me.getLeader(), req);
        }
    }


}
