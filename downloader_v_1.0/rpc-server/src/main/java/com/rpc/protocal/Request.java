package com.rpc.protocal;

import com.raft.common.Node;

public class Request<T> {

    public int getCmd() {
        return cmd;
    }

    public void setCmd(int cmd) {
        this.cmd = cmd;
    }

    public Node getSrcNode() {
        return srcNode;
    }

    public void setSrcNode(Node srcNode) {
        this.srcNode = srcNode;
    }

    public T getObj() {
        return obj;
    }

    public void setObj(T obj) {
        this.obj = obj;
    }

    private int cmd = -1;
    private Node srcNode;
    private T obj;

    public Request(int cmd, Node srcNode, T obj) {
        this.cmd = cmd;
        this.srcNode = srcNode;
        this.obj = obj;
    }
}
