package com.raft.entity;

import com.raft.common.Node;
import lombok.Data;

@Data
public class HeapPoint {
    Node node;
    int taskLeft;

    public HeapPoint(Node node, int taskLeft) {
        this.node = node;
        this.taskLeft = taskLeft;
    }

}
