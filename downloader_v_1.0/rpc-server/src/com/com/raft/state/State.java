package com.com.raft.state;

import com.com.rpc.protocal.Request;

public interface State {
    public String currentState();

    public void dealMessage(Request request);
}
