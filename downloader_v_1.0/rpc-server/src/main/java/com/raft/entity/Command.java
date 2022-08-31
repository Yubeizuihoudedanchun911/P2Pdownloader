package com.raft.entity;

import com.download.manage.SliceInfo;
import com.download.manage.SlicePageInfo;
import com.raft.common.Node;
import lombok.Data;

import java.util.Map;

@Data
public class Command {
    private  String targetUri;
    private SlicePageInfo pageInfo;
    private Map<SliceInfo,Node> taskArrangement;

    public Command(String targetUri, SlicePageInfo pageInfo, Map<SliceInfo, Node> taskArrangement) {
        this.targetUri = targetUri;
        this.pageInfo = pageInfo;
        this.taskArrangement = taskArrangement;
    }
}
