package com.entity;

import java.util.Set;

import com.common.Node;


public class GroupEntry {
    Set<Node> nodes;
    String groupID;

    public GroupEntry() {
    }

    public GroupEntry(Set<Node> nodes, String groupID) {
        this.nodes = nodes;
        this.groupID = groupID;
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
}
