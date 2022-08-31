package com.rpc.protocal;

import com.raft.common.Node;
import lombok.Data;
import org.apache.tomcat.util.net.openssl.ciphers.Protocol;
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
