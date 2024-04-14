package com.com.raft.entity;

import com.com.raft.common.Node;


public class HeapPoint {
    Node node;
    int taskLeft;

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public int getTaskLeft() {
        return taskLeft;
    }

    public void setTaskLeft(int taskLeft) {
        this.taskLeft = taskLeft;
    }

    public HeapPoint(Node node, int taskLeft) {
        this.node = node;
        this.taskLeft = taskLeft;
    }

    @Override
    public String toString() {
        return "HeapPoint{" +
                "node=" + node +
                ", taskLeft=" + taskLeft +
                '}';
    }
}
