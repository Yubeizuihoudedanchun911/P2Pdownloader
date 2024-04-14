package com.com.raft.entity;

import com.com.raft.common.Node;


public class Command<T> {
    private String targetUri;
    private T obj;
    private Node targetNode;

    public String getTargetUri() {
        return targetUri;
    }

    public void setTargetUri(String targetUri) {
        this.targetUri = targetUri;
    }

    public T getObj() {
        return obj;
    }

    public void setObj(T obj) {
        this.obj = obj;
    }

    public Node getTargetNode() {
        return targetNode;
    }

    public void setTargetNode(Node targetNode) {
        this.targetNode = targetNode;
    }

    public Command(String targetUri, T obj, Node targetNode) {
        this.targetUri = targetUri;
        this.obj = obj;
        this.targetNode = targetNode;
    }
}
