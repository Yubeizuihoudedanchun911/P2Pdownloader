package com.rpc.protocal;

import java.util.Set;

import com.raft.common.Node;


public class GroupEntry {
    Set<Node> nodes;

    public GroupEntry(Set<Node> nodes, String groupID) {
        this.nodes = nodes;
        this.groupID = groupID;
    }

    public GroupEntry() {
    }

    public Set<Node> getNodes() {
        return nodes;
    }

    public void setNodes(Set<Node> nodes) {
        this.nodes = nodes;
    }

    public String getGroupID() {
        return groupID;
    }

    public void setGroupID(String groupID) {
        this.groupID = groupID;
    }

    String groupID;
}
