package com.common;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class Group {
    private Set<Node> online_nodes;
    private String groupID;

    public Group(Set<Node> online_nodes) {
        this.online_nodes = online_nodes;
        groupID = UUID.randomUUID().toString();
    }

    public void joinGroup(Node node){
        online_nodes.add(node);
    }

    public void leftGroup(Node node){
        online_nodes.remove(node);
    }

    public void leftGroup(Set<Node> list){
        for(Node node : list){
            leftGroup(node);
        }
    }
    public int size(){
        return this.online_nodes.size();
    }

    public Set<Node> getOnline_nodes() {
        return online_nodes;
    }


    public void setOnline_nodes(Set<Node> online_nodes) {
        this.online_nodes = online_nodes;
    }

    public String getGroupID() {
        return groupID;
    }

    public void setGroupID(String groupID) {
        this.groupID = groupID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Group)) return false;
        Group group = (Group) o;
        return group.getGroupID().equals(groupID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(online_nodes, groupID);
    }
}
