package com.raft.state;

import com.rpc.protocal.Request;

public interface State {
    public String currentState();
    public void dealMessage(Request request);
}
