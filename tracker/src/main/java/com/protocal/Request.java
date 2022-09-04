package com.protocal;

import com.common.Node;
import lombok.Data;

@Data
public class Request<T> {

    private  int cmd = -1 ;
    private Node srcNode ;
    private T obj;

    public Request(int cmd, Node srcNode, T obj) {
        this.cmd = cmd;
        this.srcNode = srcNode;
        this.obj = obj;
    }
}
