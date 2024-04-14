package com.com.rpc.protocal;

//协议包
public class MessageProtocol {
    private int len; //关键
    private byte[] content;
    private int slice_idx;

    public int getLen() {
        return len;
    }

    public void setLen(int len) {
        this.len = len;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public int getSlice_idx() {
        return slice_idx;
    }

    public void setSlice_idx(int slice_idx) {
        this.slice_idx = slice_idx;
    }

    public MessageProtocol() {
    }

    public MessageProtocol(int len, byte[] content, int slice_idx) {
        this.len = len;
        this.content = content;
        this.slice_idx = slice_idx;
    }
}
