package com.api;

import static com.api.download.manage.DownLoadUtil.SLICE_SIZE;
import static com.api.download.manage.DownLoadUtil.getTotalSize;
import static com.api.download.manage.DownLoadUtil.mergeFileTranTo;
import static com.api.download.manage.DownLoadUtil.splitPage;
import static com.rpc.RPCProxy.getProxy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson2.JSON;
import com.api.download.manage.DownLoadUtil;
import com.api.download.manage.SliceInfo;
import com.api.download.manage.SlicePageInfo;
import com.db.dao.DownLoadFilePathDAO;
import com.db.entity.FilePathEntity;
import com.google.gson.reflect.TypeToken;
import com.raft.common.Node;
import com.raft.common.RaftNode;
import com.raft.entity.Command;
import com.raft.entity.HeapPoint;
import com.raft.entity.LogEntry;
import com.raft.util.TimeUtil;
import com.rpc.client.RpcClient;
import com.rpc.protocal.CommandType;
import com.rpc.protocal.MessageProtocol;
import com.rpc.protocal.Request;


@Component("downloadCenter")
public class DownLoadCenterimpl implements DownloadCenter {


    private static final Logger log = LoggerFactory.getLogger("DownLoadCenterimpl");
    private static final ExecutorService executorService =
            Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private RaftNode me;
    public Map<String, TaskListener> taskListenerMap;

    public RaftNode getMe() {
        return me;
    }

    public void setMe(RaftNode me) {
        this.me = me;
    }

    public Map<String, TaskListener> getTaskListenerMap() {
        return taskListenerMap;
    }

    public void setTaskListenerMap(Map<String, TaskListener> taskListenerMap) {
        this.taskListenerMap = taskListenerMap;
    }

    public DownLoadCenterimpl(RaftNode me) {
        this.me = me;
        taskListenerMap = new ConcurrentHashMap<>();
    }


    @Override
    public String toString() {
        return "DownLoadCenterimpl{" +
                "me=" + me +
                ", taskListenerMap=" + taskListenerMap +
                '}';
    }

    @Override
    public void downloadReq(String uri, String fileName) {
        //        Command<DownLoadReuestEntry> command = new Command<>(uri, new DownLoadReuestEntry(true, null), me
        //        .getMe());
        //        Request<Command> req = new Request<>(2, me.getMe(), command);
        RpcClient rpcClient = new RpcClient();
        TaskListener taskListener = new TaskListener(fileName, uri);
        taskListenerMap.put(uri, taskListener);
        DownloadCenter downloadCenter = (DownloadCenter) getProxy(DownloadCenter.class, me.getLeader());
        int i = downloadCenter.fullDownLoad(uri, me.getMe());
        taskListener.setTotalPages(i);
    }

    /**
     * 负载均衡： 使用小根堆 进行挑选当前任务较少的节点
     * to arrange downloadWork to each node
     */
    @Override
    public LogEntry downloadArrange(String downloadUrl, Node targetNode, SlicePageInfo slicePageInfo) {
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
        LogEntry<Command> logEntry =
                new LogEntry(me.getStateMachine().getIndex(), me.getStateMachine().getTerm(), command);
        //        Request<LogEntry> req = new Request<>(CommandType.COMMAND, me.getMe(), logEntry);
        log.info("after arranged task map :" + taskArrangement);
        return logEntry;
    }


    @Override
    public int dealDownload(LogEntry logEntry) {
        //        LogEntry logEntry = JSON.parseObject(request.getObj().toString(), LogEntry.class);
        Command command = JSON.parseObject(logEntry.getComand().toString(), Command.class);
        Node tarNode = command.getTargetNode();
        String downloadUrl = command.getTargetUri();
        Map<String, List<SliceInfo>> taskArrangement =
                JSON.parseObject(command.getObj().toString(), new TypeToken<Map<String, List<SliceInfo>>>() {
                }.getType());
        String key = me.getMe().getHost() + ":" + me.getMe().getPort();
        List<SliceInfo> list = taskArrangement.get(key);
        log.info("downLoad SLices list " + list);
        if (list == null) {
            return 0;
        }

        for (SliceInfo sliceInfo : list) {
            executorService.submit(() -> {
                File tmpFile = DownLoadUtil.download(tempPath, downloadUrl, sliceInfo,
                        "" + tarNode.getHost() + "_" + tarNode.getPort() + "-" + UUID.randomUUID());
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
                    Request<Command> req = new Request<>(CommandType.DATA_TRANSFER, me.getMe(), command1);
                    me.send(tarNode, req);
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
        return list.size();
    }

    //    //  每个下载node 将下载的总片输 回交给leader 进行管理
    //    public void sendCommandAck(Node leader, int doneTask) {
    //        Request req = new Request<Integer>(CommandType.COMMAND_ACK, me.getMe(), doneTask);
    //        me.send(leader, req);
    //    }

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
                if (taskListener.success) {
                    taskListenerMap.remove(key);
                }
                break;
            case CommandType.DOWNLOAD_ACK:
                int pages = com.alibaba.fastjson.JSON.parseObject(command.getObj().toString(), Integer.class);
                taskListener.setTotalPages(pages);

                break;
        }
    }

    @Override
    public int fullDownLoad(String uri, Node tarNode) {
        SlicePageInfo slicePageInfo = getSilcePageInfo(uri);
        LogEntry logEntry = downloadArrange(uri, tarNode, slicePageInfo);
        new Thread(() -> {
            me.broadcastToAll(logEntry);
        }).start();

        return slicePageInfo.getPages();
    }

    @Override
    public void partDownLoad(String uri, Node tarNode, CopyOnWriteArrayList<Integer> slices) {
        log.info("partDownLoad..." + slices);
        SlicePageInfo slicePageInfo = getSilcePageInfo(uri);
        SlicePageInfo failedSlices = new SlicePageInfo();
        CopyOnWriteArrayList<SliceInfo> sliceInfos = new CopyOnWriteArrayList<>();
        for (int i : slices) {
            for (SliceInfo sliceInfo : slicePageInfo.getSliceInfoList()) {
                if (sliceInfo.getPage() == i) {
                    sliceInfos.add(sliceInfo);
                }
            }
        }
        failedSlices.setSliceInfoList(sliceInfos);
        LogEntry logEntry = downloadArrange(uri, tarNode, failedSlices);
        new Thread(() -> {
            me.broadcastToAll(logEntry);
        }).start();
    }

    private class TaskListener {
        private volatile int totalPages;
        private AtomicInteger leftPages;
        private volatile boolean[] recordPages;
        private String file_name;
        private boolean success;
        private TimeUtil timeUtil;
        private final long checkTime = 1000 * 10L;
        private String uri;

        public TaskListener(String file_name, String uri) {
            this.file_name = file_name;
            success = false;
            this.uri = uri;

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
                        restTimeUtil();
                    }
                }
                DownLoadFilePathDAO downLoadFilePathDAO = new DownLoadFilePathDAO();
                downLoadFilePathDAO.save(new FilePathEntity(file_name,uri));

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
            this.leftPages = new AtomicInteger(totalPages);
            recordPages = new boolean[totalPages];
            timeUtil = new TimeUtil(checkTime);
            count();
        }


        public boolean isSuccess() {
            return success;
        }


        public void receiveSlice(Command command) {

            MessageProtocol msg = JSON.parseObject(command.getObj().toString(), MessageProtocol.class);
            restTimeUtil();
            File file = new File(tempPath, msg.getSlice_idx() + "-" + file_name);
            if (file.exists() && file.length() == SLICE_SIZE) {
                log.info("此分片文件 {} 已存在", msg.getSlice_idx());
                return;
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
                synchronized (this) {
                    this.recordPages[msg.getSlice_idx() - 1] = true;
                }
                if (!isSuccess() && leftPages.decrementAndGet() == 0) {
                    downLoadLoacalCombine();
                }

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

        private CopyOnWriteArrayList<Integer> checkSlice() {
            CopyOnWriteArrayList<Integer> slicesIndex = new CopyOnWriteArrayList<>();
            for (int i = 0; i < totalPages; i++) {
                if (!recordPages[i]) {
                    slicesIndex.add(i + 1);
                }
            }
            return slicesIndex;
        }

        private void goOnDownload() {

            new Thread(() -> {
                CopyOnWriteArrayList<Integer> slicesIndex = checkSlice();
                DownloadCenter proxy = (DownloadCenter) getProxy(DownloadCenter.class, me.getLeader());
                proxy.partDownLoad(uri, me.getMe(), slicesIndex);
            }).start();
        }
    }


}
