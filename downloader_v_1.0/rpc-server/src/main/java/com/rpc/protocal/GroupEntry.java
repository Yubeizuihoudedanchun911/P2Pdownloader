package com.rpc.protocal;

import com.raft.common.Node;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;

@Data
@AllArgsConstructor
public class GroupEntry {
    Set<Node> nodes;
    String groupID;
}
